package org.carlspring.strongbox.config;

import org.carlspring.strongbox.config.hazelcast.HazelcastConfiguration;
import org.carlspring.strongbox.config.janusgraph.EmbeddedDbServerConfiguration;
import org.carlspring.strongbox.config.janusgraph.InMemoryDbServerConfiguration;
import org.carlspring.strongbox.config.janusgraph.RemoteDbServerConfiguration;
import org.carlspring.strongbox.gremlin.adapters.EntityTraversalAdaptersConfig;
import org.carlspring.strongbox.gremlin.server.GremlinServerConfig;
import org.carlspring.strongbox.repositories.RepositoriesConfig;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;

/**
 * Spring configuration for data service project.
 *
 * @author Alex Oreshkevich
 * @author Przemyslaw Fusik
 * @author sbespalov
 */
@Configuration
@Lazy(false)
@EnableConfigurationProperties
@ConfigurationPropertiesScan
@EnableTransactionManagement(proxyTargetClass = true, order = DataServiceConfig.TRANSACTIONAL_INTERCEPTOR_ORDER)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan({ "org.carlspring.strongbox.data" })
@Import({ EmbeddedDbServerConfiguration.class,
          InMemoryDbServerConfiguration.class,
          RemoteDbServerConfiguration.class,
          GremlinServerConfig.class,
          RepositoriesConfig.class,
          EntityTraversalAdaptersConfig.class,
          HazelcastConfiguration.class})
@EnableCaching(order = 105)
public class DataServiceConfig
{

    /**
     * This must be after {@link OEntityUnproxyAspect} order.
     */
    public static final int TRANSACTIONAL_INTERCEPTOR_ORDER = 100;

    @Bean
    public CacheManager cacheManager(HazelcastInstance hazelcastInstance)
    {
        return new HazelcastCacheManager(hazelcastInstance);
    }

}
