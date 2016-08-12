package org.carlspring.strongbox.users;

import org.carlspring.strongbox.CommonConfig;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.security.encryption.EncryptionAlgorithms;
import org.carlspring.strongbox.security.jaas.Credentials;
import org.carlspring.strongbox.security.jaas.Users;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import java.io.IOException;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
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
@EnableOrientRepositories(basePackages = "org.carlspring.strongbox.users.repository")
@Import({ DataServiceConfig.class,
          CommonConfig.class })
public class UsersConfig
{

    private static final Logger logger = LoggerFactory.getLogger(UsersConfig.class);

    private final static GenericParser<Users> parser = new GenericParser<>(Users.class);

    @Autowired
    private OObjectDatabaseTx databaseTx;

    @Autowired
    private UserService userService;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ConfigurationResourceResolver configurationResourceResolver;

    @Autowired
    private AuthorizationConfigProvider authorizationConfigProvider;


    private synchronized OObjectDatabaseTx getDatabaseTx()
    {
        databaseTx.activateOnCurrentThread();
        return databaseTx;
    }

    @PostConstruct
    @Transactional
    public synchronized void init()
    {
        logger.debug("Loading users...");

        // register all domain entities
        getDatabaseTx().getEntityManager().registerEntityClasses(User.class.getPackage().getName());

        loadUsersFromConfigFile();
    }

    @Transactional
    private synchronized void loadUsersFromConfigFile()
    {
        try
        {
            // save loaded users to the database if schema do not exists
            boolean needToSaveInDb = userService.count() == 0;
            parser.parse(getUsersConfigurationResource().getURL()).getUsers().stream().forEach(
                    user -> obtainUser(user, needToSaveInDb));
        }
        catch (Exception e)
        {
            logger.error("Unable to load users from configuration file.", e);
            throw new BeanInstantiationException(getClass(), "Unable to load users from configuration file.", e);
        }
    }

    @Transactional
    private synchronized void obtainUser(org.carlspring.strongbox.security.jaas.User user,
                                         boolean needToSaveInDb)
    {
        User internalUser = toInternalUser(user);
        logger.debug("Saving new user from config file:\n\t" + user);

        if (needToSaveInDb)
        {
            internalUser = userService.save(internalUser);
            internalUser = getDatabaseTx().detach(internalUser, true);
        }

        cacheManager.getCache("users").put(internalUser.getUsername(), internalUser);
    }

    @Transactional
    private synchronized User toInternalUser(org.carlspring.strongbox.security.jaas.User user)
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
        return configurationResourceResolver.getConfigurationResource("users.config.xml",
                                                                      "etc/conf/security-users.xml");
    }

}
