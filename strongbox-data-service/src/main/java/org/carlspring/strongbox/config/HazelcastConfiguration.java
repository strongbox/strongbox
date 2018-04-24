package org.carlspring.strongbox.config;

import org.carlspring.strongbox.data.CacheName;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
public class HazelcastConfiguration
{

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
    public Config hazelcastConfig()
    {
        final Config config = new Config().setInstanceName("strongbox")
                                          .addMapConfig(newDefaultMapConfig(CacheName.User.USERS))
                                          .addMapConfig(newDefaultMapConfig(CacheName.User.USER_DETAILS))
                                          .addMapConfig(newDefaultMapConfig(CacheName.Repository.REMOTE_REPOSITORY_ALIVENESS))
                                          .addMapConfig(newDefaultMapConfig(CacheName.Artifact.TAGS));
        config.getGroupConfig().setName("strongbox").setPassword("password");
        return config;
    }
}
