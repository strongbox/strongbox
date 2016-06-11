package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.RemoteRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.lambdista.util.Try;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
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
    OObjectDatabaseTx db;

    public ConfigurationRepository()
    {
    }

    private OObjectDatabaseTx getDatabase()
    {
        db.activateOnCurrentThread();
        return db;
    }

    @PostConstruct
    public synchronized void init()
    {
        logger.info("ConfigurationRepository.init()");

        OObjectDatabaseTx db = getDatabase();

        db.getEntityManager().registerEntityClass(Configuration.class, true);
        db.getEntityManager().registerEntityClass(RemoteRepository.class);
        db.getEntityManager().registerEntityClass(Storage.class);
        db.getEntityManager().registerEntityClass(ProxyConfiguration.class);
        db.getEntityManager().registerEntityClass(RoutingRules.class);
        db.getEntityManager().registerEntityClass(RuleSet.class);
        db.getEntityManager().registerEntityClass(Repository.class);
        db.getEntityManager().registerEntityClass(RoutingRule.class);

        String propertyKey = "repository.config.xml";

        if (!schemaExists())
        {
            createSettings(propertyKey);
        }
        else
        {
            Configuration content = getConfiguration();

            if (content == null)
            {
                createSettings(propertyKey);
            }
        }
    }

    private synchronized boolean schemaExists()
    {
        OObjectDatabaseTx db = getDatabase();
        return db.getMetadata().getSchema().existsClass(Configuration.class.getSimpleName());
    }

    private synchronized void createSettings(String propertyKey)
    {
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

        updateConfiguration(configuration);
    }

    public synchronized Configuration getConfiguration()
    {
        try
        {
            OObjectDatabaseTx db = getDatabase();
            List<Configuration> result = db.query(new OSQLSynchQuery<>("SELECT * FROM Configuration"));
            if (result != null && !result.isEmpty())
            {
                Configuration configuration = result.get(result.size() - 1);
                logger.debug("Loaded configuration " + configuration);

                return db.detachAll(configuration, true);
            }
        }
        catch (Exception e)
        {
            logger.error("Unable to get configuration.", e);
            return null;
        }

        return null;
    }

    public synchronized <T> void updateConfiguration(ServerConfiguration<T> configuration)
    {
        try
        {
            OObjectDatabaseTx db = getDatabase();
            db.save(configuration);
        }
        catch (Exception e)
        {
            logger.error("Unable to update configuration.", e);
        }
    }

}
