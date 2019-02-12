package org.carlspring.strongbox.config;

import org.apache.commons.lang.StringUtils;
import org.carlspring.strongbox.data.server.InMemoryOrientDbServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.strongbox.db.server.OrientDbServer;
import org.strongbox.db.server.OrientDbServerConfiguration;

import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Conditional(InMemoryOrientDbConfig.class)
class InMemoryOrientDbConfig
        extends CommonOrientDbConfig
        implements Condition
{

    private static final String ORIENTDB_DEFAULT_USERNAME = "admin";

    private static final String ORIENTDB_DEFAULT_PASSWORD = "admin";

    private static final Logger logger = LoggerFactory.getLogger(InMemoryOrientDbConfig.class);

    @Bean(destroyMethod = "close")
    @DependsOn("orientDbServer")
    OrientDB orientDB(OrientDbServerConfiguration serverProperties)
    {
        String database = serverProperties.getDatabase();
        logger.info(String.format("Initialize In-Memory OrientDB server for [%s]", database));

        OrientDB orientDB = new OrientDB(StringUtils.substringBeforeLast(serverProperties.getUrl(), "/"),
                                         serverProperties.getUsername(),
                                         serverProperties.getPassword(),
                                         getOrientDBConfig());
        if (!orientDB.exists(database))
        {
            logger.info(String.format("Creating database [%s]...", database));
            orientDB.create(database, ODatabaseType.MEMORY);

            try (ODatabaseSession session = orientDB.open(database, ORIENTDB_DEFAULT_USERNAME,
                                                          ORIENTDB_DEFAULT_PASSWORD))
            {
                session.command("UPDATE ouser SET password = :password WHERE name = :name",
                                new Object[]{ serverProperties.getPassword(),
                                              serverProperties.getUsername() });
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
        OrientDBProfile profile = OrientDBProfile.resolveProfile(conditionContext.getEnvironment());
        
        return profile.getName().equals(OrientDBProfile.PROFILE_MEMORY);
    }
}
