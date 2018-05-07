package org.carlspring.strongbox.config;

import org.carlspring.strongbox.data.server.InMemoryOrientDbServer;
import org.carlspring.strongbox.data.server.OrientDbServer;

import javax.inject.Inject;

import com.orientechnologies.orient.core.config.OGlobalConfiguration;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Conditional(InMemoryOrientDbConfig.class)
class InMemoryOrientDbConfig
        implements Condition
{

    private static final String ORIENTDB_DEFAULT_USERNAME = "admin";

    private static final String ORIENTDB_DEFAULT_PASSWORD = "admin";

    private static final Logger logger = LoggerFactory.getLogger(InMemoryOrientDbConfig.class);

    private OrientDBConfig orientDBConfig = OrientDBConfig.builder()
                                                          .addConfig(OGlobalConfiguration.DB_POOL_MIN, 1L)
                                                          .addConfig(OGlobalConfiguration.DB_POOL_MAX, 10L)
                                                          .build();

    @Inject
    private ConnectionConfig connectionConfig;

    @Bean(destroyMethod = "close")
    @DependsOn("orientDbServer")
    OrientDB orientDB()
    {
        String database = connectionConfig.getDatabase();
        logger.info(String.format("Initialize In-Memory OrientDB server for [%s]", database));

        OrientDB orientDB = new OrientDB(connectionConfig.getUrl(), connectionConfig.getUsername(),
                                         connectionConfig.getPassword(), orientDBConfig);
        if (!orientDB.exists(database))
        {
            logger.info(String.format("Creating database [%s]...", database));
            orientDB.create(database, ODatabaseType.MEMORY);

            try (ODatabaseSession session = orientDB.open(database, ORIENTDB_DEFAULT_USERNAME,
                                                          ORIENTDB_DEFAULT_PASSWORD))
            {
                session.command("UPDATE ouser SET password = :password WHERE name = :name",
                                new Object[]{ connectionConfig.getPassword(),
                                              connectionConfig.getUsername() });
                session.commit();
            }
        }

        return orientDB;
    }

    @Bean
    OrientDbServer orientDbServer()
    {
        return new InMemoryOrientDbServer();
    }

    @Override
    public boolean matches(ConditionContext conditionContext,
                           AnnotatedTypeMetadata metadata)

    {
        return ConnectionConfigOrientDB.resolveProfile(conditionContext.getEnvironment())
                                       .equals(ConnectionConfigOrientDB.PROFILE_MEMORY);
    }
}
