package org.carlspring.strongbox.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.carlspring.strongbox.data.CacheManagerConfiguration;
import org.carlspring.strongbox.data.domain.GenericEntity;
import org.carlspring.strongbox.data.server.EmbeddedOrientDbServer;
import org.carlspring.strongbox.data.server.OrientDbServer;
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
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
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
@Import(DataServicePropertiesConfig.class)
@EnableCaching(order = 105)
public class DataServiceConfig
{

    /**
     * This must be after {@link OEntityUnproxyAspect} order.
     */
    public static final int TRANSACTIONAL_INTERCEPTOR_ORDER = 100;

    @Inject
    private ConnectionConfig connectionConfig;
    
    @Inject
    private OrientDbServer orientDbServer;
    
    @PostConstruct
    public void init()
        throws Exception
    {
        orientDbServer.start();
        
        transactionTemplate().execute((s) ->
                                    {
                                        doInit(s);
                                        return null;
                                    });
    }
    
    @PreDestroy
    public void destroy() {
        orientDbServer.stop();
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
        jpaProperties.put("javax.persistence.jdbc.url", connectionConfig.getUrl());
        jpaProperties.put("javax.persistence.jdbc.user", connectionConfig.getUsername());
        jpaProperties.put("javax.persistence.jdbc.password", connectionConfig.getPassword());

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
        return OEntityManager.getEntityManagerByDatabaseURL(connectionConfig.getUrl());
    }

    @Bean
    public CacheManager cacheManager(net.sf.ehcache.CacheManager cacheManager)
    {
        EhCacheCacheManager result = new EhCacheCacheManager(cacheManager);
        result.setTransactionAware(true);
        return result;
    }

    @Bean
    public CacheManagerConfiguration cacheManagerConfiguration()
    {
        CacheManagerConfiguration cacheManagerConfiguration = new CacheManagerConfiguration();
        cacheManagerConfiguration.setCacheCacheManagerId("strongboxCacheManager");
        return cacheManagerConfiguration;
    }
    
    @Bean
    public EhCacheManagerFactoryBean ehCacheCacheManager(CacheManagerConfiguration cacheManagerConfiguration)
    {
        EhCacheManagerFactoryBean cmfb = new EhCacheManagerFactoryBean();
        cmfb.setConfigLocation(new ClassPathResource("ehcache.xml"));
        cmfb.setShared(false);
        cmfb.setCacheManagerName(cacheManagerConfiguration.getCacheCacheManagerId());
        return cmfb;
    }
    
}
