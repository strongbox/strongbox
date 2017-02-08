package org.carlspring.strongbox.config;

import org.carlspring.strongbox.data.server.EmbeddedOrientDbServer;
import org.carlspring.strongbox.data.tx.CustomOrientTransactionManager;

import javax.annotation.PostConstruct;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.orient.commons.core.OrientTransactionManager;
import org.springframework.data.orient.object.OrientObjectDatabaseFactory;
import org.springframework.data.orient.object.OrientObjectTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Spring configuration for data service project.
 *
 * @author Alex Oreshkevich
 */
@Configuration
@EnableTransactionManagement
@ComponentScan({ "org.carlspring.strongbox.data" })
@Import(DataServicePropertiesConfig.class)
@EnableCaching
public class DataServiceConfig
{

    private static final Logger logger = LoggerFactory.getLogger("DataServiceConfig");

    @Value("${strongbox.orientdb.host:127.0.0.1}")
    String host;

    @Value("${strongbox.orientdb.port:2424}")
    Integer port;

    @Value("${strongbox.orientdb.database:strongbox}")
    String database;

    @Value("${strongbox.orientdb.username:admin}")
    String username;

    @Value("${strongbox.orientdb.password:password}")
    String password;

    private static EmbeddedOrientDbServer embeddableServer;

    @Bean
    @Lazy
    public OrientObjectDatabaseFactory factory()
    {
        OrientObjectDatabaseFactory factory = new OrientObjectDatabaseFactory();
        factory.setUrl(getConnectionUrl());
        factory.setUsername(username);
        factory.setPassword(password);

        return factory;
    }

    @Bean
    @Lazy
    public OrientTransactionManager transactionManager()
    {
        return new CustomOrientTransactionManager(factory());
    }

    @Bean
    @Lazy
    public OrientObjectTemplate objectTemplate()
    {
        return new OrientObjectTemplate(factory());
    }

    @Bean
    @Lazy
    public OObjectDatabaseTx objectDatabaseTx()
    {
        return factory().db();
    }

    @Bean
    @Lazy
    public CacheManager cacheManager()
    {
        return new EhCacheCacheManager(ehCacheCacheManager().getObject());
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheCacheManager()
    {
        EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
        cmfb.setConfigLocation(new ClassPathResource("ehcache.xml"));
        cmfb.setShared(true);
        return cmfb;
    }

    @PostConstruct
    public void registerEntities()
            throws Exception
    {
        if (embeddableServer == null)
        {
            embeddableServer = new EmbeddedOrientDbServer(this);
            embeddableServer.init();
        }

        embeddableServer.start();

        // create database if not initialized
        OServerAdmin serverAdmin = new OServerAdmin(getConnectionUrl()).connect(username, password);
        if (!serverAdmin.existsDatabase())
        {
            logger.debug("Create database " + database);
            serverAdmin.createDatabase(database, "document", "plocal")/*.close()*/;
        }
        else
        {
            logger.debug("Reuse existing database " + database);
        }
    }

    private String getConnectionUrl()
    {
        return "remote:" + host + ":" + port + "/" + database;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public Integer getPort()
    {
        return port;
    }

    public void setPort(Integer port)
    {
        this.port = port;
    }

    public String getDatabase()
    {
        return database;
    }

    public void setDatabase(String database)
    {
        this.database = database;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

}
