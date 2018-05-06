package org.carlspring.strongbox.config;

import org.carlspring.strongbox.data.server.OrientDbServer;
import org.carlspring.strongbox.data.tx.OEntityUnproxyAspect;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import com.hazelcast.core.HazelcastInstance;
import com.orientechnologies.orient.core.db.ODatabasePool;
import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.core.sql.OCommandExecutorSQLFactory;
import com.orientechnologies.orient.core.sql.functions.OSQLFunctionFactory;
import com.orientechnologies.orient.jdbc.OrientDataSource;
import com.orientechnologies.orient.object.jpa.OrientDbJpaVendorAdapter;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.reflections.Reflections;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.ReflectionUtils;

/**
 * Spring configuration for data service project.
 *
 * @author Alex Oreshkevich
 */
@Configuration
@Lazy(false)
@EnableTransactionManagement(proxyTargetClass = true, order = DataServiceConfig.TRANSACTIONAL_INTERCEPTOR_ORDER)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan({ "org.carlspring.strongbox.data" })
@Import({ DataServicePropertiesConfig.class,
          HazelcastConfiguration.class })
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
    private ResourceLoader resourceLoader;

    @Inject
    private OrientDbServer server;


    @PostConstruct
    public void init() throws ClassNotFoundException, LiquibaseException
    {
        //server.start();

        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource(pool()));
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.xml");
        liquibase.afterPropertiesSet();

        OEntityManager oEntityManager = oEntityManager();
        // register all domain entities
        Stream.concat(new Reflections("org.carlspring.strongbox").getTypesAnnotatedWith(Entity.class).stream(),
                      new Reflections("org.carlspring.strongbox").getTypesAnnotatedWith(Embeddable.class).stream())
              .forEach(oEntityManager::registerEntityClass);
    }

    @PreDestroy
    public void preDestroy()
    {
        server.stop();
    }

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf)
    {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(ODatabasePool pool)
    {
        /*
        Map<String, String> jpaProperties = new HashMap<>();
        jpaProperties.put("javax.persistence.jdbc.url", connectionConfig.getUrl());
        jpaProperties.put("javax.persistence.jdbc.user", connectionConfig.getUsername());
        jpaProperties.put("javax.persistence.jdbc.password", connectionConfig.getPassword());
        */
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        //emf.set
        //emf.setDataSource(dataSource);

        JpaVendorAdapter vendorAdapter = new OrientDbJpaVendorAdapter(pool);
        emf.setJpaVendorAdapter(vendorAdapter);
        //emf.setJpaProperties(additionalProperties());

        return emf;
    }

    @Bean
    public OEntityManager oEntityManager()
    {
        return OEntityManager.getEntityManagerByDatabaseURL(connectionConfig.getUrl());
    }

    @Bean
    public CacheManager cacheManager(HazelcastInstance hazelcastInstance)
    {
        HazelcastTransactionSupportingCacheManager cacheManager = new HazelcastTransactionSupportingCacheManager(hazelcastInstance);
        cacheManager.setTransactionAware(true);
        return cacheManager;
    }

    @Bean
    public DataSource dataSource(ODatabasePool pool)
    {
        ServiceLoader.load(OSQLFunctionFactory.class);
        ServiceLoader.load(OCommandExecutorSQLFactory.class);

        final OrientDataSource ds = new OrientDataSource(server.orientDB());

        // OrientDataSource bug no1
        Field poolField = ReflectionUtils.findField(OrientDataSource.class, "pool");
        ReflectionUtils.makeAccessible(poolField);
        ReflectionUtils.setField(poolField, ds, pool);

        // OrientDataSource bug no2
        Field infoField = ReflectionUtils.findField(OrientDataSource.class, "info");
        ReflectionUtils.makeAccessible(infoField);
        ReflectionUtils.setField(infoField, ds, new Properties());
        return ds;
    }

    @Bean
    public ODatabasePool pool()
    {
        return new ODatabasePool(server.orientDB(), connectionConfig.getDatabase(), connectionConfig.getUsername(),
                                 connectionConfig.getPassword());
    }

}
