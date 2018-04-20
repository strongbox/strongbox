package org.carlspring.strongbox.config;

import org.carlspring.strongbox.data.CacheManagerConfiguration;
import org.carlspring.strongbox.data.server.OrientDbServer;
import org.carlspring.strongbox.data.tx.OEntityUnproxyAspect;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.transaction.HazelcastTransactionManager;
import com.hazelcast.spring.transaction.ManagedTransactionalTaskContext;
import com.orientechnologies.orient.core.entity.OEntityManager;
import com.orientechnologies.orient.core.sql.OCommandExecutorSQLFactory;
import com.orientechnologies.orient.core.sql.functions.OSQLFunctionFactory;
import liquibase.exception.LiquibaseException;
import liquibase.integration.spring.SpringLiquibase;
import org.reflections.Reflections;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;
import static org.springframework.transaction.support.AbstractPlatformTransactionManager.SYNCHRONIZATION_ON_ACTUAL_TRANSACTION;

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
    private ResourceLoader resourceLoader;
    
    @Inject
    private OrientDbServer server;

    @PostConstruct
    public void init() throws ClassNotFoundException, LiquibaseException
    {
        server.start();
        
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource());
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setChangeLog("classpath:/db/changelog/db.changelog-master.xml");
        liquibase.afterPropertiesSet();

        new TransactionTemplate(transactionManager()).execute((s) -> {
            OEntityManager oEntityManager = oEntityManager();
            // register all domain entities
            Stream.concat(new Reflections("org.carlspring.strongbox").getTypesAnnotatedWith(Entity.class).stream(),
                          new Reflections("org.carlspring.strongbox").getTypesAnnotatedWith(Embeddable.class).stream())
                  .forEach(oEntityManager::registerEntityClass);
            
            return null;
        });
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
    public OEntityManager oEntityManager()
    {
        return OEntityManager.getEntityManagerByDatabaseURL(connectionConfig.getUrl());
    }

    @Bean
    public CacheManagerConfiguration cacheManagerConfiguration()
    {
        CacheManagerConfiguration cacheManagerConfiguration = new CacheManagerConfiguration();
        cacheManagerConfiguration.setCacheCacheManagerId("strongboxCacheManager");

        return cacheManagerConfiguration;
    }

    @Bean
    public HazelcastInstance hazelcastInstance()
    {
        Config config = new Config();
        config.getGroupConfig().setName("strongbox");
        config.getGroupConfig().setPassword("password");

        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    public CacheManager cacheManager()
    {
        return new HazelcastCacheManager(hazelcastInstance());
    }

    @Bean
    public HazelcastTransactionManager transactionManager()
    {
        HazelcastTransactionManager hazelcastTransactionManager = new HazelcastTransactionManager(hazelcastInstance());
        hazelcastTransactionManager.setTransactionSynchronization(SYNCHRONIZATION_ON_ACTUAL_TRANSACTION);

        return hazelcastTransactionManager;
    }

    @Bean
    public ManagedTransactionalTaskContext transactionalContext()
    {
        return new ManagedTransactionalTaskContext(transactionManager());
    }

    @Bean
    public DataSource dataSource()
        throws ClassNotFoundException
    {
        Class.forName("com.orientechnologies.orient.jdbc.OrientJdbcDriver");

        ServiceLoader.load(OSQLFunctionFactory.class);
        ServiceLoader.load(OCommandExecutorSQLFactory.class);

        SingleConnectionDataSource ds = new SingleConnectionDataSource();
        ds.setAutoCommit(false);
        ds.setUsername(connectionConfig.getUsername());
        ds.setPassword(connectionConfig.getPassword());
        ds.setUrl(String.format("jdbc:orient:%s", connectionConfig.getUrl()));

        return ds;
    }

}
