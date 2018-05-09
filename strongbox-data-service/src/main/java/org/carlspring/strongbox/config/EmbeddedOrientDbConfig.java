package org.carlspring.strongbox.config;

import org.carlspring.strongbox.data.server.EmbeddedOrientDbServer;
import org.carlspring.strongbox.data.server.OrientDbServer;

import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import org.apache.commons.lang.StringUtils;
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
@Conditional(EmbeddedOrientDbConfig.class)
class EmbeddedOrientDbConfig
        extends CommonOrientDbConfig
        implements Condition
{

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedOrientDbConfig.class);

    @Bean(destroyMethod = "close")
    @DependsOn("orientDbServer")
    OrientDB orientDB()
    {
        OrientDB orientDB = new OrientDB(StringUtils.substringBeforeLast(connectionConfig.getUrl(), "/"),
                                         connectionConfig.getUsername(),
                                         connectionConfig.getPassword(),
                                         getOrientDBConfig());
        String database = connectionConfig.getDatabase();

        if (!orientDB.exists(database))
        {
            logger.info(String.format("Creating database [%s]...", database));

            orientDB.create(database, ODatabaseType.PLOCAL);
        }
        else
        {
            logger.info("Reuse existing database " + database);
        }
        return orientDB;
    }

    @Bean
    OrientDbServer orientDbServer()
    {
        return new EmbeddedOrientDbServer();
    }

    @Override
    public boolean matches(ConditionContext conditionContext,
                           AnnotatedTypeMetadata metadata)

    {
        return ConnectionConfigOrientDB.resolveProfile(conditionContext.getEnvironment())
                                       .equals(ConnectionConfigOrientDB.PROFILE_EMBEDDED);
    }
}
