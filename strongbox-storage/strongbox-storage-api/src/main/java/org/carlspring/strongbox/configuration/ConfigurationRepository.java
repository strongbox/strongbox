package org.carlspring.strongbox.configuration;


import com.google.common.collect.Iterables;
import com.lambdista.util.Try;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.RemoteRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;
import org.carlspring.strongbox.xml.parsers.GenericParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

import static org.carlspring.strongbox.db.DbUtils.withDatabase;

@Component("strongboxConfigurationRepository")
public class ConfigurationRepository {
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationRepository.class);

    public ConfigurationRepository() {
        init();
    }

    public Configuration getConfiguration()
    {
        return withDatabase(db -> {
            List<Configuration> result = db.query(new OSQLSynchQuery<>("select * from Configuration"));
            Configuration configuration = !result.isEmpty() ? Iterables.getLast(result) : null;

            return db.<Configuration>detachAll(configuration, true);
        });
    }

    public <T> void updateConfiguration(ServerConfiguration<T> configuration) {
        withDatabase(db -> {
            db.save(configuration);
        });
    }

    boolean schemaExists() {
        return withDatabase(db -> {
            return db.getMetadata().getSchema().existsClass("Configuration");
        });
    }

    public void init()
    {
        logger.info("ConfigurationRepository.init()");

        withDatabase(db -> {
            db.getEntityManager().registerEntityClass(Configuration.class, true);
            db.getEntityManager().registerEntityClass(RemoteRepository.class);
            db.getEntityManager().registerEntityClass(Storage.class);
            db.getEntityManager().registerEntityClass(ProxyConfiguration.class);
            db.getEntityManager().registerEntityClass(RoutingRules.class);
            db.getEntityManager().registerEntityClass(RuleSet.class);
            db.getEntityManager().registerEntityClass(Repository.class);
            db.getEntityManager().registerEntityClass(RoutingRule.class);
        });

        String propertyKey = "repository.config.xml";

        if (!schemaExists()) {
            createSettings(propertyKey);
        } else {
            Configuration content = getConfiguration();

            if (content == null) {
                createSettings(propertyKey);
            }
        }
    }

    private void createSettings(String propertyKey) {
        if (System.getProperty(propertyKey) != null)
        {
            String filename = System.getProperty(propertyKey);
            GenericParser<Configuration> parser = new GenericParser<>(Configuration.class);
            Configuration configuration = Try.apply(() -> parser.parse(new File(filename))).get();

            withDatabase(db -> {
                db.save(configuration);
            });
        }
    }
}
