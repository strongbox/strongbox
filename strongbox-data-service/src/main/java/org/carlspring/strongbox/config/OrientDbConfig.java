package org.carlspring.strongbox.config;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Properties;

import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.jdbc.OrientDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.ReflectionUtils;
import org.strongbox.db.server.OrientDbServerConfiguration;

import org.carlspring.strongbox.config.orientdb.EmbeddedOrientDbConfig;
import org.carlspring.strongbox.config.orientdb.InMemoryOrientDbConfig;

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

    /**
     * The pool size is declared in either:
     * - org.carlspring.strongbox.config.EmbeddedOrientDbConfig#orientDBConfig
     * - org.carlspring.strongbox.config.InMemoryOrientDbConfig#orientDBConfig
     */
    @Bean(destroyMethod = "close")
    ODatabasePool databasePool(OrientDbServerConfiguration serverProperties)
    {
        return new ODatabasePool(orientDB,
                                 serverProperties.getDatabase(),
                                 serverProperties.getUsername(),
                                 serverProperties.getPassword());
    }

}
