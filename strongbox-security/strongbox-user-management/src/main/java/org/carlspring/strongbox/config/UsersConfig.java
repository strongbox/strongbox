package org.carlspring.strongbox.config;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.security.Credentials;
import org.carlspring.strongbox.security.UserAccessModel;
import org.carlspring.strongbox.security.UserPathPermissions;
import org.carlspring.strongbox.security.UserRepository;
import org.carlspring.strongbox.security.Users;
import org.carlspring.strongbox.security.encryption.EncryptionAlgorithms;
import org.carlspring.strongbox.users.domain.AccessModel;
import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Spring configuration for all user-related code.
 *
 * @author Alex Oreshkevich
 */
@Configuration
@ComponentScan({ "org.carlspring.strongbox.users" })
@EnableTransactionManagement(proxyTargetClass = true,
                             order = DataServiceConfig.TRANSACTIONAL_INTERCEPTOR_ORDER)
@Import({ DataServiceConfig.class,
          CommonConfig.class })
public class UsersConfig
{

    private static final Logger logger = LoggerFactory.getLogger(UsersConfig.class);

    private final static GenericParser<Users> parser = new GenericParser<>(Users.class);

    @Inject
    private OEntityManager oEntityManager;

    @Inject
    private UserService userService;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private TransactionTemplate transactionTemplate;

    private final Class<User> userClass = User.class;

    @PostConstruct
    public void init()
    {
        logger.debug("Loading users...");

        transactionTemplate.execute((s) ->
                                    {
                                        doInit();
                                        return null;
                                    });
    }

    private void doInit()
    {
        // register all domain entities
        oEntityManager.registerEntityClasses(User.class.getPackage()
                                                       .getName());

        // set unique constraints and index field 'username' if it isn't present yet
        OClass oUserClass = ((OObjectDatabaseTx) entityManager.getDelegate()).getMetadata()
                                                                             .getSchema()
                                                                             .getOrCreateClass(
                                                                                     userClass.getSimpleName());

        if (oUserClass.getIndexes()
                      .stream()
                      .noneMatch(oIndex -> oIndex.getName().equals("idx_username")))
        {
            oUserClass.createProperty("username", OType.STRING);
            oUserClass.createIndex("idx_username", OClass.INDEX_TYPE.UNIQUE, "username");
        }

        // remove all possible existing users (due to test executions with @Rollback(false) or another causes)
        // just to make sure
        userService.deleteAll();

        loadUsersFromConfigFile();
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

        Credentials credentials = user.getCredentials();
        EncryptionAlgorithms algorithms = EncryptionAlgorithms.valueOf(credentials.getEncryptionAlgorithm()
                                                                                  .toUpperCase());

        switch (algorithms)
        {
            case PLAIN:
                internalUser.setPassword(credentials.getPassword());
                break;

            // TODO process other cases
            default:
                throw new UnsupportedOperationException(algorithms.toString());
        }

        internalUser.setEnabled(true);
        internalUser.setRoles(user.getRoles());
        internalUser.setSalt(user.getSeed() + "");

        // load userAccessModel
        UserAccessModel userAccessModel = user.getUserAccessModel();
        if (userAccessModel != null)
        {
            AccessModel internalAccessModel = new AccessModel();
            userAccessModel.getStorages()
                           .getStorages()
                           .forEach(storage ->
                                            storage.getRepositories()
                                                   .getRepositories()
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
                  .getPrivileges()
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
        return ConfigurationResourceResolver.getConfigurationResource("users.config.xml",
                                                                      "etc/conf/strongbox-security-users.xml");
    }

}
