package org.carlspring.strongbox.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.carlspring.strongbox.data.domain.GenericEntity;
import org.carlspring.strongbox.data.server.EmbeddedOrientDbServer;
import org.carlspring.strongbox.data.tx.OEntityUnproxyAspect;
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
import org.springframework.orm.jpa.EntityManagerFactoryUtils;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import com.orientechnologies.orient.client.remote.OServerAdmin;
import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

/**
 * Spring configuration for data service project.
 *
 * @author Alex Oreshkevich
 */
@Configuration
@EnableTransactionManagement(proxyTargetClass = true, order = DataServiceConfig.TRANSACTIONAL_INTERCEPTOR_ORDER)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan({ "org.carlspring.strongbox.data" })
@Import({DataServicePropertiesConfig.class, CustomTypesConfig.class})
@EnableCaching(order = 105)
public class DataServiceConfig
{

    /**
     * This must be after {@link OEntityUnproxyAspect} order.
     */
    public static final int TRANSACTIONAL_INTERCEPTOR_ORDER = 100;

    private static final Logger logger = LoggerFactory.getLogger("DataServiceConfig");

    @Value("${strongbox.orientdb.host:127.0.0.1}")
    private String host;

    @Value("${strongbox.orientdb.port:2424}")
    private Integer port;

    @Value("${strongbox.orientdb.database:strongbox}")
    private String database;

    @Value("${strongbox.orientdb.username:admin}")
    private String username;

    @Value("${strongbox.orientdb.password:password}")
    private String password;

    private static EmbeddedOrientDbServer embeddableServer;

    @PostConstruct
    public void init()
        throws Exception
    {
        startDbServer();
        transactionTemplate().execute((s) ->
                                    {
                                        doInit(s);
                                        return null;
                                    });
    }

    private void doInit(TransactionStatus s)
    {
        oEntityManager().registerEntityClass(GenericEntity.class);
        EntityManager entityManager = EntityManagerFactoryUtils.getTransactionalEntityManager(entityManagerFactory());
        OClass oGenericEntityClass = ((OObjectDatabaseTx) entityManager.getDelegate()).getMetadata()
                                                                                      .getSchema()
                                                                                      .getOrCreateClass(GenericEntity.class.getSimpleName());
        
        if (oGenericEntityClass.getIndexes()
                      .stream()
                      .noneMatch(oIndex -> oIndex.getName().equals("idx_uuid")))
        {
            oGenericEntityClass.createIndex("idx_uuid", OClass.INDEX_TYPE.UNIQUE, "uuid");
        }
    }    
    
    @Bean
    public PlatformTransactionManager transactionManager()
    {
        return new JpaTransactionManager((EntityManagerFactory)entityManagerFactory());
    }

    @Bean
    public EntityManagerFactory entityManagerFactory()
    {
        Map<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put("javax.persistence.jdbc.url", getConnectionUrl());
        jpaProperties.put("javax.persistence.jdbc.user", username);
        jpaProperties.put("javax.persistence.jdbc.password", password);

        LocalContainerEntityManagerFactoryBean result = new LocalContainerEntityManagerFactoryBean();
        result.setJpaPropertyMap(jpaProperties);
        result.afterPropertiesSet();
        return result.getObject();
    }

    @Bean
    public TransactionTemplate transactionTemplate()
    {
        TransactionTemplate result = new TransactionTemplate();
        result.setTransactionManager(transactionManager());
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

    private void startDbServer()
        throws Exception
    {
        logger.info(String.format("Start Embedded OrientDB server [%s].", getConnectionUrl()));
        if (embeddableServer == null)
        {
            embeddableServer = new EmbeddedOrientDbServer(this);
            embeddableServer.init();
        }

        try
        {
            embeddableServer.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

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
