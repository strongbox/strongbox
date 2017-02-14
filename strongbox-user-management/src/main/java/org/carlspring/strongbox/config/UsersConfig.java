package org.carlspring.strongbox.config;

import org.carlspring.strongbox.data.service.NoProxyOrientRepositoryFactoryBean;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.security.Credentials;
import org.carlspring.strongbox.security.Users;
import org.carlspring.strongbox.security.encryption.EncryptionAlgorithms;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.io.IOException;

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
import org.springframework.data.orient.commons.repository.config.EnableOrientRepositories;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring configuration for all user-related code.
 *
 * @author Alex Oreshkevich
 */
@Configuration
@ComponentScan({ "org.carlspring.strongbox.users" })
@EnableOrientRepositories(basePackages = "org.carlspring.strongbox.users.repository",
                          repositoryFactoryBeanClass = NoProxyOrientRepositoryFactoryBean.class)
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
    
    private final Class<User> userClass = User.class;

    @PostConstruct
    public void init()
    {
        logger.debug("Loading users...");

        // register all domain entities
        oEntityManager.registerEntityClasses(User.class.getPackage()
                                                   .getName());

        // set unique constraints and index field 'username' if it isn't present yet
        OClass oUserClass = ((OObjectDatabaseTx)entityManager.getDelegate()).getMetadata()
                                      .getSchema()
                                      .getOrCreateClass(userClass.getSimpleName());

        if (!oUserClass.getIndexes()
                       .stream()
                       .filter(oIndex -> oIndex.getName()
                                               .equals("idx_username"))
                       .findAny()
                       .isPresent())
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
            internalUser = userService.save(internalUser);
            logger.debug("Saving new user from config file:\n\t" + internalUser);
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

        return internalUser;
    }

    private Resource getUsersConfigurationResource()
            throws IOException
    {
        return ConfigurationResourceResolver.getConfigurationResource("users.config.xml",
                                                                      "etc/conf/security-users.xml");
    }

}
