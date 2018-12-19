package org.carlspring.strongbox.config;

import org.carlspring.strongbox.data.server.InMemoryOrientDbServer;
import org.carlspring.strongbox.data.server.OrientDbServer;

import javax.sql.DataSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.zip.GZIPInputStream;

import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabaseType;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.tool.ODatabaseImport;
import com.orientechnologies.orient.core.metadata.security.OSecurityShared;
import com.orientechnologies.orient.jdbc.OrientJdbcConnection;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
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
    protected ODatabaseDocumentInternal databaseDocument(DataSource dataSource)
            throws IOException
    {
        Connection connection;
        try
        {
            connection = dataSource.getConnection();
        }
        catch (SQLException e)
        {
            throw new UndeclaredThrowableException(e);
        }

        ODatabaseDocumentInternal database = (ODatabaseDocumentInternal) ((OrientJdbcConnection) connection).getDatabase();

        ODatabaseImport oDatabaseImport = new ODatabaseImport(database,
                                                              new GZIPInputStream(new BufferedInputStream(
                                                                      new ClassPathResource(
                                                                              "db/export/strongbox.export-20181218.gz").getInputStream())),
                                                              iText -> logger.info(iText));
        try
        {
            oDatabaseImport.importDatabase();
        }
        finally
        {
            oDatabaseImport.close();
        }

        ((OSecurityShared) database.getMetadata().getSecurity().getUnderlying()).createMetadata();

        return database;
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
