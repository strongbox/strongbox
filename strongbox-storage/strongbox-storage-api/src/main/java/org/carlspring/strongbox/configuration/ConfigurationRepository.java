package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.services.ServerConfigurationService;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.util.Optional;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("configurationRepository")
@Transactional
public class ConfigurationRepository
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationRepository.class);
    @Autowired
    ServerConfigurationService serverConfigurationService;
    @Autowired
    ConfigurationCache configurationCache;
    @Autowired
    private OObjectDatabaseTx databaseTx;
    private String currentDatabaseId;

    public ConfigurationRepository()
    {
    }

    private synchronized OObjectDatabaseTx getDatabase()
    {
        databaseTx.activateOnCurrentThread();
        return databaseTx;
    }

    @PostConstruct
    public synchronized void init()
    {
        logger.info("ConfigurationRepository.init()");
        getDatabase().getEntityManager().registerEntityClass(BinaryConfiguration.class, true);

        if (!schemaExists() || getConfiguration() == null)
        {
            createSettings("repository.config.xml");
        }
    }

    private synchronized boolean schemaExists()
    {
        OObjectDatabaseTx db = getDatabase();
        return db != null && db.getMetadata().getSchema().existsClass(BinaryConfiguration.class.getSimpleName());
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

        GenericParser<Configuration> parser = configurationCache.getParser();
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

    @Transactional
    public synchronized Optional<Configuration> updateConfiguration(Configuration configuration)
    {
        if (configuration == null)
        {
            throw new NullPointerException("configuration is null");
        }

        try
        {
            final String data = configurationCache.getParser().serialize(configuration);
            final String configurationId = configuration.getId();

            // update existing configuration with new data (if possible)
            if (configurationId != null)
            {
                serverConfigurationService.findOne(configurationId).ifPresent(
                        binaryConfiguration -> doSave(binaryConfiguration, data));
            }
            else
            {
                doSave(new BinaryConfiguration(), data);
            }

            if (currentDatabaseId == null)
            {
                throw new NullPointerException("currentDatabaseId is null");
            }

            configuration.setId(currentDatabaseId);
            configurationCache.save(configuration);

            logger.debug("Configuration updated under ID " + currentDatabaseId);
        }
        catch (Exception e)
        {
            logger.error("Unable to save configuration\n\n" + configuration, e);
            return Optional.empty();
        }

        return Optional.of(configuration);
    }

    @Transactional
    private synchronized void doSave(BinaryConfiguration binaryConfiguration,
                                     String data)
    {
        binaryConfiguration.setData(data);
        binaryConfiguration = serverConfigurationService.save(binaryConfiguration);
        currentDatabaseId = binaryConfiguration.getId();
    }
}
