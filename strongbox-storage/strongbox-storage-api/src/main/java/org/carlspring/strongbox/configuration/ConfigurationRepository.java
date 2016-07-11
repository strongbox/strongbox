package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.services.ServerConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.HttpConnectionPool;
import org.carlspring.strongbox.storage.repository.RemoteRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.util.Optional;

import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.core.serialization.serializer.object.OObjectSerializer;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.orientechnologies.orient.object.serialization.OObjectSerializerContext;
import com.orientechnologies.orient.object.serialization.OObjectSerializerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.orient.object.OrientObjectDatabaseFactory;
import org.springframework.stereotype.Component;

@Component("configurationRepository")
public class ConfigurationRepository
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationRepository.class);

    @Autowired
    private OrientObjectDatabaseFactory factory;

    private OObjectDatabaseTx databaseTx;

    @Autowired
    ServerConfigurationService serverConfigurationService;

    @Autowired
    ConfigurationCache configurationCache;

    private String currentDatabaseId;

    public ConfigurationRepository()
    {
    }

    private synchronized OObjectDatabaseTx getDatabase()
    {
        return getDatabase(true);
    }

    private synchronized OObjectDatabaseTx getDatabase(boolean activate)
    {
        if (activate)
        {
            databaseTx.activateOnCurrentThread();
        }
        return databaseTx;
    }

    @PostConstruct
    public synchronized void init()
    {
        logger.info("ConfigurationRepository.init()");
        OObjectDatabaseTx db = databaseTx = factory.db();
        ODatabase oDatabase = db.activateOnCurrentThread();

        OEntityManager entityManager = databaseTx.getEntityManager();

        entityManager.registerEntityClass(Configuration.class);
        entityManager.registerEntityClass(RemoteRepository.class);
        entityManager.registerEntityClass(HttpConnectionPool.class);
        entityManager.registerEntityClass(Storage.class);
        entityManager.registerEntityClass(ProxyConfiguration.class);
        entityManager.registerEntityClass(RoutingRules.class);
        entityManager.registerEntityClass(RuleSet.class);
        entityManager.registerEntityClass(Repository.class);
        entityManager.registerEntityClass(RoutingRule.class);

        //databaseTx.getMetadata().getSchema().synchronizeSchema();

        checkRegisteredEntities();

        final GenericParser<Configuration> configurationParser = configurationCache.getParser();
        OObjectSerializerContext serializerContext = new OObjectSerializerContext();
        serializerContext.bind(new OObjectSerializer<Configuration, String>()
        {
            @Override
            public Configuration unserializeFieldValue(Class aClass,
                                                       String o)
            {
                System.out.println("\n\n\nCUSTOM DESERIALIZER!!!!!\n\n\n");
                try
                {
                    return configurationParser.deserialize(o);
                }
                catch (Exception e)
                {
                    logger.error("Unable to deserialize configuration", e);
                    return null;
                }
            }

            @Override
            public String serializeFieldValue(Class aClass,
                                              Configuration o)
            {
                System.out.println("\n\n\nCUSTOM SERIALIZER!!!!!\n\n\n");
                try
                {
                    return configurationParser.serialize((Configuration) o);
                }
                catch (Exception e)
                {
                    logger.error("Unable to serialize configuration", e);
                    return null;
                }
            }
        }, oDatabase);

        OObjectSerializerHelper.register();
        OObjectSerializerHelper.bindSerializerContext(null, serializerContext);

        checkRegisteredEntities();


        if (!schemaExists() || getConfiguration() == null)
        {
            createSettings("repository.config.xml");
        }
    }

    private void checkRegisteredEntities(){
        final boolean[] found = { false };
        databaseTx.getEntityManager().getRegisteredEntities().forEach(aClass -> found[0] |= aClass.getSimpleName().equals(Configuration.class.getSimpleName()));
        if (!found[0])
        {
            logger.error("All registered entities:");
            databaseTx.getEntityManager().getRegisteredEntities().forEach(aClass -> logger.error(aClass.getName()));
            throw new RuntimeException("Configuration entity was not registered to serialization context");
        }
    }

    private synchronized boolean schemaExists()
    {
        OObjectDatabaseTx db = getDatabase();
        return db != null && db.getMetadata().getSchema().existsClass(Configuration.class.getSimpleName());
    }

    private synchronized void createSettings(String propertyKey)
    {
        // skip configuration initialization if config is already in place
        if (currentDatabaseId != null)
        {
            logger.debug("Skip config initialization: already in place.");
            return;
        }

        logger.debug("Load configuration from XML file...");

        GenericParser<Configuration> parser = new GenericParser<>(Configuration.class);
        String filename = System.getProperty(propertyKey);
        Configuration configuration = null;

        if (filename != null)
        {
            File file = new File(filename);
            try
            {
                configuration = parser.parse(file);
            }
            catch (Exception e)
            {
                logger.error("Unable to parse configuration from file", e);
            }
        }
        else
        {
            InputStream is = getClass().getClassLoader().getResourceAsStream("etc/conf/strongbox.xml");
            try
            {
                configuration = parser.parse(is);
            }
            catch (Exception e)
            {
                logger.error("Unable to parse configuration from InputStream", e);
            }
        }

        // Create configuration in database and put it to cache.
        updateConfiguration(configuration);
    }

    public synchronized Configuration getConfiguration()
    {
        Optional<Configuration> optionalConfig = configurationCache.getConfiguration(currentDatabaseId);
        if (optionalConfig.isPresent())
        {
            return optionalConfig.get();
        }

        return null;
    }

    public synchronized Optional<Configuration> updateConfiguration(Configuration configuration)
    {
        if (configuration == null)
        {
            throw new NullPointerException("configuration is null");
        }

        System.out.println("Trying to save " + configuration);

        try
        {
            OObjectDatabaseTx databaseTx = getDatabase();
            databaseTx.getMetadata().reload();
            databaseTx.getEntityManager().registerEntityClass(Configuration.class, true);

            configuration = databaseTx.detach(serverConfigurationService.save(configuration), true);
            System.out.println("SAVED " + configuration);

            configuration = configurationCache.save(configuration);
            currentDatabaseId = configuration.getId();
            logger.debug("Configuration updated under ID " + currentDatabaseId);
        }
        catch (Exception e)
        {
            logger.error("Unable to save configuration\n\n" + configuration, e);
            return Optional.empty();
        }

        return Optional.of(configuration);
    }
}
