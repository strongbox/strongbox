package org.carlspring.strongbox.config;

import org.carlspring.strongbox.data.tx.OEntityUnproxyAspect;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import com.hazelcast.core.HazelcastInstance;
import com.orientechnologies.orient.object.jpa.OJPAObjectDatabaseTxPersistenceProvider;
import liquibase.integration.spring.SpringLiquibase;
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
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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

    @Bean
    public SpringLiquibase springLiquibase(ResourceLoader resourceLoader)
    {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.xml");
        return liquibase;
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf)
    {
        return new JpaTransactionManager(emf);
    }

    @Bean
    @DependsOn("springLiquibase")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(ConnectionConfig connectionConfig)
    {
        Map<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put("javax.persistence.jdbc.url", connectionConfig.getUrl());
        jpaProperties.put("javax.persistence.jdbc.user", connectionConfig.getUsername());
        jpaProperties.put("javax.persistence.jdbc.password", connectionConfig.getPassword());

        LocalContainerEntityManagerFactoryBean result = new LocalContainerEntityManagerFactoryBean();
        result.setJpaPropertyMap(jpaProperties);
        result.setDataSource(dataSource);
        result.setPersistenceUnitName("strongbox-PU");
        result.setPackagesToScan("org.carlspring.strongbox");
        result.setPersistenceProvider(new OJPAObjectDatabaseTxPersistenceProvider());

        // @sbespalov, needed ?
        // ServiceLoader.load(OSQLFunctionFactory.class);
        // ServiceLoader.load(OCommandExecutorSQLFactory.class);

        return result;
    }

    @Bean
    public CacheManager cacheManager(HazelcastInstance hazelcastInstance)
    {
        HazelcastTransactionSupportingCacheManager cacheManager = new HazelcastTransactionSupportingCacheManager(hazelcastInstance);
        cacheManager.setTransactionAware(true);
        return cacheManager;
    }


}
