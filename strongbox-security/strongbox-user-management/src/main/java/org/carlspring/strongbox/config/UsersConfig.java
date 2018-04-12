package org.carlspring.strongbox.config;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.security.UserAccessModel;
import org.carlspring.strongbox.security.UserPathPermissions;
import org.carlspring.strongbox.security.UserRepository;
import org.carlspring.strongbox.security.Users;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.xml.parsers.GenericParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.orientechnologies.orient.core.entity.OEntityManager;

/**
 * Spring configuration for all user-related code.
 *
 * @author Alex Oreshkevich
 */
@Configuration
@ComponentScan({ "org.carlspring.strongbox.users" })
@Import({ DataServiceConfig.class,
          CommonConfig.class})
public class UsersConfig
{

    private static final Logger logger = LoggerFactory.getLogger(UsersConfig.class);

    private final static GenericParser<Users> parser = new GenericParser<>(Users.class);

    @Inject
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private PlatformTransactionManager transactionManager;


    @PostConstruct
    public void init()
    {
        logger.debug("Loading users...");

        new TransactionTemplate(transactionManager).execute((s) -> doInit());
    }

    private Object doInit()
    {
        // remove all possible existing users (due to test executions with @Rollback(false) or another causes)
        // just to make sure
        userService.deleteAll();

        loadUsersFromConfigFile();
        
        return null;
    }

    @Transactional
    private void loadUsersFromConfigFile()
    {
        try
        {
            // save loaded users to the database if schema do not exists
            boolean needToSaveInDb = userService.count() == 0;
            parser.parse(getUsersConfigurationResource().getURL())
                  .getUsers()
                  .forEach(user -> obtainUser(user, needToSaveInDb));
        }
        catch (Exception e)
        {
            logger.error("Unable to load users from configuration file.", e);
            throw new BeanInstantiationException(getClass(), "Unable to load users from configuration file.", e);
        }
    }

    @Transactional
    private void obtainUser(org.carlspring.strongbox.security.User user,
                            boolean needToSaveInDb)
    {
        User internalUser = toInternalUser(user);
        if (needToSaveInDb)
        {
            logger.debug("Saving new user from config file:\n\t" + internalUser);

            try
            {
                userService.save(internalUser);
            }
            catch (Exception e)
            {
                logger.error("Unable to save user " + internalUser.getUsername(), e);
            }
        }
    }

    @Transactional
    private User toInternalUser(org.carlspring.strongbox.security.User user)
    {
        User internalUser = new User();
        internalUser.setUsername(user.getUsername());
        internalUser.setPassword(user.getPassword());
        internalUser.setEnabled(true);
        internalUser.setRoles(user.getRoles());

        // load userAccessModel
        UserAccessModel userAccessModel = user.getUserAccessModel();
        if (userAccessModel != null)
        {
            AccessModel internalAccessModel = new AccessModel();
            userAccessModel.getStorages()
                           .forEach(storage ->
                                            storage.getRepositories()
                                                   .forEach(repository -> processRepository(internalAccessModel,
                                                                                            storage.getStorageId(),
                                                                                            repository)));
            internalUser.setAccessModel(internalAccessModel);
        }

        if (user.getSecurityTokenKey() != null && !user.getSecurityTokenKey().trim().isEmpty())
        {
            internalUser.setSecurityTokenKey(user.getSecurityTokenKey());
        }

        return internalUser;
    }

    private void processRepository(AccessModel internalAccessModel,
                                   String storageId,
                                   UserRepository repository)
    {
        // assign default repository-level privileges set
        Set<String> defaultPrivileges = new HashSet<>();
        String key = "/storages/" + storageId + "/" + repository.getRepositoryId();

        repository.getPrivileges()
                  .forEach(privilege -> defaultPrivileges.add(privilege.getName().toUpperCase()));

        internalAccessModel.getRepositoryPrivileges().put(key, defaultPrivileges);

        // assign path-specific privileges
        UserPathPermissions userPathPermissions = repository.getPathPermissions();
        if (userPathPermissions != null)
        {

            userPathPermissions
                    .getPathPermissions()
                    .forEach(pathPermission ->
                             {
                                 Set<String> privileges = translateToPrivileges(pathPermission.getPermission());
                                 internalAccessModel.getUrlToPrivilegesMap()
                                                    .put(key + "/" + pathPermission.getPath(), privileges);
                             });
            internalAccessModel.obtainPrivileges();
        }
    }

    private Set<String> translateToPrivileges(String permission)
    {
        if (permission == null || permission.equalsIgnoreCase(Privileges.DEFAULT))
        {
            return Privileges.rw();
        }
        else
        {
            return Privileges.r();
        }
    }

    private Resource getUsersConfigurationResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("strongbox.users.config.xml",
                                                                      "etc/conf/strongbox-security-users.xml");
    }

}
