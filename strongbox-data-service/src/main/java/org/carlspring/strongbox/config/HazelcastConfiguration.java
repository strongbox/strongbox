package org.carlspring.strongbox.config;

import java.util.Set;

import org.carlspring.strongbox.data.CacheName;
import org.carlspring.strongbox.data.domain.EntitySerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionConfig.MaxSizePolicy;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.config.SerializationConfig;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
public class HazelcastConfiguration
{

    public static final int ARTIFACT_ENTRY_CACHE_INVALIDATE_INTERVAL = 60;
    public static final int AUTHENTICATION_CACHE_INVALIDATE_INTERVAL = 10;

    public static MapConfig authenticationCacheConfig(String name)
    {
        return new MapConfig().setName(name).setNearCacheConfig(new NearCacheConfig().setCacheLocalEntries(true)
                                                                                     .setEvictionConfig(new EvictionConfig().setMaximumSizePolicy(MaxSizePolicy.ENTRY_COUNT)
                                                                                                                            .setSize(1000))
                                                                                     .setInvalidateOnChange(true)
                                                                                     .setTimeToLiveSeconds(AUTHENTICATION_CACHE_INVALIDATE_INTERVAL));
    }

    public static MapConfig artifactEntryCacheConfig(String name)
    {
        return new MapConfig().setName(name).setNearCacheConfig(new NearCacheConfig().setCacheLocalEntries(true)
                                                                                     .setEvictionConfig(new EvictionConfig().setMaximumSizePolicy(MaxSizePolicy.ENTRY_COUNT)
                                                                                                                            .setSize(100))
                                                                                     .setInvalidateOnChange(true)
                                                                                     .setTimeToLiveSeconds(ARTIFACT_ENTRY_CACHE_INVALIDATE_INTERVAL));
    }

    public static MapConfig newDefaultMapConfig(String name)
    {
        return new MapConfig().setName(name)
                              .setMaxSizeConfig(new MaxSizeConfig(1000, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
                              .setEvictionPolicy(EvictionPolicy.LFU);
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config config)
    {
        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    public SerializationConfig hazelcastSerializationConfig(@Autowired(required = false) Set<EntitySerializer<?>> entitySerializerSet)
    {
        SerializationConfig serializationConfig = new SerializationConfig();
        if (entitySerializerSet == null) {
            return serializationConfig;
        }
        
        for (EntitySerializer<?> entitySerializer : entitySerializerSet)
        {
            serializationConfig.getSerializerConfigs()
                               .add(new SerializerConfig().setTypeClass(entitySerializer.getEntityClass())
                                                          .setImplementation(entitySerializer));
        }

        return serializationConfig;
    }

    @Bean
    public Config hazelcastConfig(SerializationConfig serializationConfig)
    {
        final Config config = new Config().setInstanceName("strongbox")
                                          .addMapConfig(newDefaultMapConfig(CacheName.Repository.REMOTE_REPOSITORY_ALIVENESS))
                                          .addMapConfig(newDefaultMapConfig(CacheName.Artifact.TAGS))
                                          .addMapConfig(authenticationCacheConfig(CacheName.User.AUTHENTICATIONS))
                                          .addMapConfig(artifactEntryCacheConfig(CacheName.Artifact.ARTIFACT_ENTRIES));
        config.getGroupConfig().setName("strongbox").setPassword("password");
        config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);

        config.setClassLoader(Thread.currentThread().getContextClassLoader());

        config.setSerializationConfig(serializationConfig);

        return config;
    }

}
