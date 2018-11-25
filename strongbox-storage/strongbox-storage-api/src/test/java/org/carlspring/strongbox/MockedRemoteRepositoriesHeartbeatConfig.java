package org.carlspring.strongbox;

import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoriesHeartbeatMonitorInitiator;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessCacheManager;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import static org.mockito.Matchers.any;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
public class MockedRemoteRepositoriesHeartbeatConfig
{

    @Primary
    @Bean(name = "mockedRemoteRepositoryAlivenessCacheManager")
    RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager()
    {
        RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager = Mockito.mock(
                RemoteRepositoryAlivenessCacheManager.class);
        Mockito.when(remoteRepositoryAlivenessCacheManager.isAlive(any(RemoteRepository.class))).thenReturn(true);
        return remoteRepositoryAlivenessCacheManager;
    }

    @Primary
    @Bean(name = "mockedRemoteRepositoriesHeartbeatMonitorInitiator")
    RemoteRepositoriesHeartbeatMonitorInitiator remoteRepositoriesHeartbeatMonitorInitiator()
    {
        return Mockito.mock(RemoteRepositoriesHeartbeatMonitorInitiator.class);
    }


}
