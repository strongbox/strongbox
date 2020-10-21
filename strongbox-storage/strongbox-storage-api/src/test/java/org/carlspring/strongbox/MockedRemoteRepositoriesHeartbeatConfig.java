package org.carlspring.strongbox;

import static org.mockito.ArgumentMatchers.any;

import javax.inject.Named;

import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoriesHeartbeatMonitorInitiator;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessService;
import org.mockito.Mockito;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.ThreadLocalTargetSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.event.AfterTestExecutionEvent;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
public class MockedRemoteRepositoriesHeartbeatConfig
{

    @Bean
    ThreadLocalTargetSource remoteRepositoryAlivenessCacheManagerTargetSource() {
        ThreadLocalTargetSource targetSource = new ThreadLocalTargetSource()
        {

            @Override
            protected Object newPrototypeInstance()
                throws BeansException
            {
                RemoteRepositoryAlivenessService mock = Mockito.mock(RemoteRepositoryAlivenessService.class);
                resetMock(mock);

                return mock;
            }

            @Override
            public void setBeanFactory(BeanFactory beanFactory)
                throws BeansException
            {
            }

        };
        targetSource.setTargetClass(RemoteRepositoryAlivenessService.class);
        
        return targetSource;
    }
    
    @Primary
    @Bean(name = "mockedRemoteRepositoryAlivenessCacheManager")
    ProxyFactoryBean remoteRepositoryAlivenessCacheManagerFactory(@Named("remoteRepositoryAlivenessCacheManagerTargetSource") ThreadLocalTargetSource targetSource)
    {
        ProxyFactoryBean proxyFactory = new ProxyFactoryBean();
        proxyFactory.setTargetSource(targetSource);

        return proxyFactory;
    }

    @Bean
    MockedRemoteRepositoryAlivenessTestExecutionListener mockedRemoteRepositoryAlivenessTestExecutionListener(@Named("remoteRepositoryAlivenessCacheManagerTargetSource") ThreadLocalTargetSource targetSource)
    {
        return new MockedRemoteRepositoryAlivenessTestExecutionListener(targetSource);
    }
    
    @Primary
    @Bean(name = "mockedRemoteRepositoriesHeartbeatMonitorInitiator")
    RemoteRepositoriesHeartbeatMonitorInitiator remoteRepositoriesHeartbeatMonitorInitiator()
    {
        return Mockito.mock(RemoteRepositoriesHeartbeatMonitorInitiator.class);
    }
    
    private static void resetMock(RemoteRepositoryAlivenessService mock)
    {
        Mockito.reset(mock);
        Mockito.when(mock.isAlive(any(RemoteRepository.class)))
               .thenReturn(Boolean.TRUE);
    }

    private static class MockedRemoteRepositoryAlivenessTestExecutionListener {

        private final TargetSource targetSource;

        public MockedRemoteRepositoryAlivenessTestExecutionListener(TargetSource targetSource)
        {
            this.targetSource = targetSource;
        }

        @EventListener(AfterTestExecutionEvent.class)
        public void afterTestExecution() throws Exception
        {
            resetMock((RemoteRepositoryAlivenessService) targetSource.getTarget());
        }
        
    }

}
