package org.carlspring.strongbox.config;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration;
import org.carlspring.strongbox.data.tx.OEntityUnproxyAspect;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.strongbox.db.server.OrientDbServerConfiguration;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.hazelcast.spring.transaction.HazelcastTransactionManager;
import com.orientechnologies.orient.object.jpa.OJPAObjectDatabaseTxPersistenceProvider;

import liquibase.integration.spring.SpringLiquibase;

/**
 * Spring configuration for data service project.
 *
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 */
@Configuration
@Lazy(false)
@EnableTransactionManagement(proxyTargetClass = true, order = DataServiceConfig.TRANSACTIONAL_INTERCEPTOR_ORDER)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan({ "org.carlspring.strongbox.data" })
@Import({ OrientDbConfig.class,
          HazelcastConfiguration.class })
@EnableCaching(order = 105)
public class DataServiceConfig
{

    /**
     * This must be after {@link OEntityUnproxyAspect} order.
     */
    public static final int TRANSACTIONAL_INTERCEPTOR_ORDER = 100;

    @Inject
    private DataSource dataSource;

    @Bean(name = "liquibase")
    public SpringLiquibase springLiquibase(ResourceLoader resourceLoader)
    {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.xml");
        return liquibase;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf, HazelcastInstance hazelcastInstance)
    {
        JpaTransactionManager jpaTransactionManager = new JpaTransactionManager(emf);
        HazelcastTransactionManager hazelcastTransactionManager = new HazelcastTransactionManager(hazelcastInstance);
        return new ChainedTransactionManager(hazelcastTransactionManager, jpaTransactionManager);
    }

    @Bean
    @DependsOn("liquibase")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(OrientDbServerConfiguration serverProperties)
    {
        Map<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put("javax.persistence.jdbc.url", serverProperties.getUrl());
        jpaProperties.put("javax.persistence.jdbc.user", serverProperties.getUsername());
        jpaProperties.put("javax.persistence.jdbc.password", serverProperties.getPassword());

        LocalContainerEntityManagerFactoryBean result = new LocalContainerEntityManagerFactoryBean();
        result.setJpaPropertyMap(jpaProperties);
        result.setDataSource(dataSource);
        result.setPersistenceUnitName("strongbox-PU");
        result.setPackagesToScan("org.carlspring.strongbox");
        result.setPersistenceProvider(new OJPAObjectDatabaseTxPersistenceProvider());

        return result;
    }

    @Bean
    public CacheManager cacheManager(HazelcastInstance hazelcastInstance)
    {
        return new HazelcastCacheManager(hazelcastInstance);
    }

}
