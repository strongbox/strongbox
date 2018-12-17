package org.carlspring.strongbox.config;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.orientechnologies.orient.core.command.OCommandOutputListener;
import com.orientechnologies.orient.core.db.ODatabaseDocumentInternal;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.tool.ODatabaseImport;
import com.orientechnologies.orient.jdbc.OrientDataSource;
import com.orientechnologies.orient.jdbc.OrientJdbcConnection;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
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

    @Inject
    private OrientDB orientDB;

    @Bean
    DataSource dataSource(ODatabasePool pool)
            throws IOException
    {
        OrientDataSource ds = new OrientDataSource(orientDB);

        ds.setInfo(new Properties());

        // DEV note:
        // NPEx hotfix for OrientDataSource.java:134 :)
        Field poolField = ReflectionUtils.findField(OrientDataSource.class, "pool");
        ReflectionUtils.makeAccessible(poolField);
        ReflectionUtils.setField(poolField, ds, pool);

        init(ds);

        return ds;
    }

    void init(DataSource dataSource)
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


        OCommandOutputListener listener = iText -> System.out.print(iText);

        ODatabaseImport import2 = new ODatabaseImport(
                (ODatabaseDocumentInternal) ((OrientJdbcConnection) connection).getDatabase(),
                "C:\\Users\\fuss\\Downloads\\strongbox.export.gz", listener);
        try
        {
            import2.importDatabase();

        }
        finally
        {
            import2.close();
        }
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
