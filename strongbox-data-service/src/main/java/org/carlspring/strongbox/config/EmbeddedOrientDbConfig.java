package org.carlspring.strongbox.config;

import org.carlspring.strongbox.data.server.EmbeddedOrientDbServer;
import org.carlspring.strongbox.data.server.OrientDbServer;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.orientechnologies.orient.core.db.OrientDB;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.FileSystemUtils;

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

    @Value("${strongbox.server.database.path:strongbox-vault/db}")
    private String databasePath;

    @Value("${strongbox.database.snapshot.resource:db-snapshot}")
    private Resource databaseSnapshotResource;

    @Bean(destroyMethod = "close")
    @DependsOn("orientDbServer")
    OrientDB orientDB()
            throws IOException
    {
        OrientDB orientDB = new OrientDB(StringUtils.substringBeforeLast(connectionConfig.getUrl(), "/"),
                                         connectionConfig.getUsername(),
                                         connectionConfig.getPassword(),
                                         getOrientDBConfig());
        String database = connectionConfig.getDatabase();

        if (!orientDB.exists(database))
        {
            logger.info(String.format("Database does not exist. Copying fresh database snapshot from [%s]...",
                                      databaseSnapshotResource.getURI()));

            Path snapshotPath = Paths.get(databaseSnapshotResource.getURI());
            FileSystemUtils.copyRecursively(snapshotPath, Paths.get(databasePath));
        }
        else
        {
            logger.info("Re-using existing database " + database + ".");
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
