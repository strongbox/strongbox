package org.carlspring.strongbox.repositories;

import org.carlspring.strongbox.StorageApiTestConfig;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.config.hazelcast.HazelcastInstanceId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Import({ DataServiceConfig.class, StorageApiTestConfig.class })
public class RepositoriesTestConfig
{

    @Primary
    @Bean
    public HazelcastInstanceId hazelcastInstanceIdAcctit()
    {
        return new HazelcastInstanceId("RepositoriesTestConfig-hazelcast-instance");
    }

}
