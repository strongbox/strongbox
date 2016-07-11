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

import com.lambdista.util.Try;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("configurationRepository")
public class ConfigurationRepository
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationRepository.class);

    @Autowired
    private OObjectDatabaseTx databaseTx;

    @Autowired
    ServerConfigurationService serverConfigurationService;

    @Autowired
    ConfigurationCache configurationCache;

    private String currentDatabaseId;

    /*
        FIXME Currently save to DB is disabled because of internal OrientDB issue: https://github.com/orientechnologies/orientdb/issues/5905
        --------------------------------------------------------------------------------------------------------------------------------
        Caused by: java.lang.IllegalArgumentException: null key not supported by embedded map
	        at com.orientechnologies.orient.core.db.record.OTrackedMap.put(OTrackedMap.java:73)
     */
    private volatile boolean saveToDbEnabled = false;

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

        OObjectDatabaseTx db = getDatabase();
        if (db != null)
        {
            db.getEntityManager().registerEntityClass(Configuration.class, true);
            db.getEntityManager().registerEntityClass(RemoteRepository.class);
            db.getEntityManager().registerEntityClass(HttpConnectionPool.class);
            db.getEntityManager().registerEntityClass(Storage.class);
            db.getEntityManager().registerEntityClass(ProxyConfiguration.class);
            db.getEntityManager().registerEntityClass(RoutingRules.class);
            db.getEntityManager().registerEntityClass(RuleSet.class);
            db.getEntityManager().registerEntityClass(Repository.class);
            db.getEntityManager().registerEntityClass(RoutingRule.class);
        }

        if (!schemaExists() || getConfiguration() == null)
        {
            createSettings("repository.config.xml");
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
        Configuration configuration;

        if (filename != null)
        {
            File file = new File(filename);
            configuration = Try.apply(() -> parser.parse(file)).get();
        }
        else
        {
            InputStream is = getClass().getClassLoader().getResourceAsStream("etc/conf/strongbox.xml");
            configuration = Try.apply(() -> parser.parse(is)).get();
        }

        // Create configuration in database and put it to cache.
        if (saveToDbEnabled)
        {
            configuration = getDatabase().detach(serverConfigurationService.save(configuration), true);
        }
        currentDatabaseId = configurationCache.save(configuration).getId();

        logger.debug("Configuration created under ID " + currentDatabaseId);
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
        if (saveToDbEnabled)
        {
            configuration = serverConfigurationService.save(configuration);
        }
        configuration = configurationCache.save(configuration);

        return Optional.of(configuration);
    }
}
