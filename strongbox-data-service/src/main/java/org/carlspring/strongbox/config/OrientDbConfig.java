package org.carlspring.strongbox.config;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.tool.ODatabaseImport;
import com.orientechnologies.orient.jdbc.OrientDataSource;
import com.orientechnologies.orient.jdbc.OrientJdbcConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ReflectionUtils;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@Import({ DataServicePropertiesConfig.class,
          InMemoryOrientDbConfig.class,
          EmbeddedOrientDbConfig.class })
class OrientDbConfig
{

    private static final Logger logger = LoggerFactory.getLogger(OrientDbConfig.class);

    @Inject
    private OrientDB orientDB;

    @Bean
    DataSource dataSource(ODatabasePool pool)
    {
        OrientDataSource ds = new OrientDataSource(orientDB);

        ds.setInfo(new Properties());

        // DEV note:
        // NPEx hotfix for OrientDataSource.java:134 :)
        Field poolField = ReflectionUtils.findField(OrientDataSource.class, "pool");
        ReflectionUtils.makeAccessible(poolField);
        ReflectionUtils.setField(poolField, ds, pool);

        return ds;
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
                                                                              "db/snapshot/strongbox-db-snapshot-20181223.json.gz").getInputStream())),
                                                              iText -> {
                                                              });
        try
        {
            oDatabaseImport.importDatabase();
        }
        finally
        {
            oDatabaseImport.close();
        }

        return database;
    }

    /**
     * The pool size is declared in either:
     * - org.carlspring.strongbox.config.EmbeddedOrientDbConfig#orientDBConfig
     * - org.carlspring.strongbox.config.InMemoryOrientDbConfig#orientDBConfig
     */
    @Bean(destroyMethod = "close")
    ODatabasePool databasePool(ConnectionConfig connectionConfig)
    {
        return new ODatabasePool(orientDB,
                                 connectionConfig.getDatabase(),
                                 connectionConfig.getUsername(),
                                 connectionConfig.getPassword());
    }

}
