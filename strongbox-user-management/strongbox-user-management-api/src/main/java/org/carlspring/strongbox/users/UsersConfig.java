package org.carlspring.strongbox.users;

import org.carlspring.strongbox.CommonConfig;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.security.encryption.EncryptionAlgorithms;
import org.carlspring.strongbox.security.jaas.Credentials;
import org.carlspring.strongbox.security.jaas.Users;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import java.io.File;
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
@Import({DataServiceConfig.class, CommonConfig.class})
public class UsersConfig
{

    private static final Logger logger = LoggerFactory.getLogger(UsersConfig.class);

    @Autowired
    OObjectDatabaseTx databaseTx;

    @Autowired
    UserService userService;

    @Autowired
    CacheManager cacheManager;

    @Autowired
    ConfigurationResourceResolver configurationResourceResolver;

    private GenericParser<Users> parser = new GenericParser<>(Users.class);

    @PostConstruct
    @Transactional
    public synchronized void init()
    {
        logger.debug("Configure users...");

        // register all domain entities
        databaseTx.getEntityManager().registerEntityClasses(User.class.getPackage().getName());

        loadUsersFromConfigFile();
    }

    @Transactional
    private synchronized void loadUsersFromConfigFile()
    {
        try
        {
            // save loaded users to the database if schema do not exists
            boolean needToSaveInDb = userService.count() == 0;
            parser.parse(getUsersConfigFile()).getUsers().stream().forEach(user -> obtainUser(user, needToSaveInDb));
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
        if (needToSaveInDb)
        {
            internalUser = userService.save(internalUser);
            databaseTx.activateOnCurrentThread();
            internalUser = databaseTx.detach(internalUser, true);
        }

        cacheManager.getCache("users").put(internalUser.getUsername(), internalUser);
    }


    @Transactional
    private synchronized User toInternalUser(org.carlspring.strongbox.security.jaas.User user)
    {
        User internalUser = new User();
        internalUser.setUsername(user.getUsername());

        Credentials credentials = user.getCredentials();
        EncryptionAlgorithms algorithms = EncryptionAlgorithms.valueOf(
                credentials.getEncryptionAlgorithm().toUpperCase());
        switch (algorithms)
        {
            case PLAIN:
                internalUser.setPassword(credentials.getPassword());
                break;
            // TODO process other cases
        }
        internalUser.setEnabled(true);
        internalUser.setRoles(user.getRoles());
        internalUser.setSalt(user.getSeed() + "");
        return internalUser;
    }

    private synchronized File getUsersConfigFile()
            throws IOException
    {
        return configurationResourceResolver.getConfigurationResource("users.config.xml", "etc/conf/security-users.xml").getFile();
    }

}
