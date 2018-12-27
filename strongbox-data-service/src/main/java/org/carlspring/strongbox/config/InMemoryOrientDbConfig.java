package org.carlspring.strongbox.config;

import org.carlspring.strongbox.data.server.InMemoryOrientDbServer;
import org.carlspring.strongbox.data.server.OrientDbServer;

import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Conditional(InMemoryOrientDbConfig.class)
class InMemoryOrientDbConfig
        extends CommonOrientDbConfig
        implements Condition
{

    private static final Logger logger = LoggerFactory.getLogger(InMemoryOrientDbConfig.class);

    @Bean(destroyMethod = "close")
    @DependsOn("orientDbServer")
    OrientDB orientDB()
    {
        String database = connectionConfig.getDatabase();
        logger.info(String.format("Initialize In-Memory OrientDB server for [%s]", database));

        OrientDB orientDB = new OrientDB(StringUtils.substringBeforeLast(connectionConfig.getUrl(), "/"),
                                         connectionConfig.getUsername(),
                                         connectionConfig.getPassword(),
                                         getOrientDBConfig());

        if (!orientDB.exists(database))
        {
            logger.info(String.format("Creating database [%s]...", database));
            orientDB.create(database, ODatabaseType.MEMORY);
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
