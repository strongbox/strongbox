package org.carlspring.strongbox.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

import org.carlspring.strongbox.data.server.EmbeddedOrientDbServer;
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
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.entity.OEntityManager;

/**
 * Spring configuration for data service project.
 *
 * @author Alex Oreshkevich
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true, order = 100)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan({ "org.carlspring.strongbox.data" })
@Import(DataServicePropertiesConfig.class)
@EnableCaching(order = 105)
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
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf)
    {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory()
    {
        Map<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put("javax.persistence.jdbc.url", getConnectionUrl());
        jpaProperties.put("javax.persistence.jdbc.user", username);
        jpaProperties.put("javax.persistence.jdbc.password", password);

        LocalContainerEntityManagerFactoryBean result = new LocalContainerEntityManagerFactoryBean();
        result.setJpaPropertyMap(jpaProperties);
        return result;
    }

    // @Bean(destroyMethod = "") // prevents to call close() on non-activated member of connection pool
    // @Lazy
    // public OObjectDatabaseTx objectDatabaseTx()
    // {
    // return factory().db();
    // }

    @Bean
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager)
    {
        TransactionTemplate result = new TransactionTemplate();
        result.setTransactionManager(transactionManager);
        return result;
    }

    @Bean
    public OEntityManager oEntityManager()
    {
        return OEntityManager.getEntityManagerByDatabaseURL(getConnectionUrl());
    }

    @Bean
    public CacheManager cacheManager()
    {
        EhCacheCacheManager result = new EhCacheCacheManager(ehCacheCacheManager().getObject());
        result.setTransactionAware(true);
        return result;
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
            serverAdmin.createDatabase(database, "document", "plocal")/* .close() */;
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
