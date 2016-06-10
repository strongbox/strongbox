package org.carlspring.strongbox.service.impl;

import org.carlspring.strongbox.config.ClientPropertiesConfig;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.data.domain.PoolConfiguration;
import org.carlspring.strongbox.data.service.PoolConfigurationService;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * @author korest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ProxyRepositoryConnectionPoolConfigurationServiceImplIntegrationTest
{

    @Configuration
    @ComponentScan(basePackages = { "org.carlspring.strongbox.service.impl"})
    @Import({DataServiceConfig.class, ClientPropertiesConfig.class})
    public static class SpringConfig
    {
        @Bean
        public PoolConfigurationService poolConfigurationService()
        {
            PoolConfigurationService poolConfigurationService = Mockito.mock(PoolConfigurationService.class);
            Mockito.when(poolConfigurationService.findAll()).thenReturn(Optional.empty());
            Mockito.when(poolConfigurationService
                    .createOrUpdateNumberOfConnectionsForRepository(Mockito.anyString(), Mockito.anyInt()))
                    .thenReturn(new PoolConfiguration());

            return poolConfigurationService;
        }
    }

    @Autowired
    private PoolConfigurationService poolConfigurationService;

    @Autowired
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @Test
    public void setMaxPoolSize()
    {
        proxyRepositoryConnectionPoolConfigurationService.setMaxTotal(10);
        assertEquals(10, proxyRepositoryConnectionPoolConfigurationService.getTotalStats().getMax());
    }

    @Test
    public void setDefaultMaxPerRepository()
    {
        proxyRepositoryConnectionPoolConfigurationService.setDefaultMaxPerRepository(8);
        assertEquals(8, proxyRepositoryConnectionPoolConfigurationService.getDefaultMaxPerRepository());
    }

    @Test
    public void setMaxPerRepository()
    {
        String repositoryUrl = "http://repo.spring.io/snapshot";
        proxyRepositoryConnectionPoolConfigurationService.setMaxPerRepository(repositoryUrl, 3);
        assertEquals(3, proxyRepositoryConnectionPoolConfigurationService.getPoolStats(repositoryUrl).getMax());
    }

    // integration test, external call to repo
    @Test
    public void connectionsReleasedTest()
    {
        String repositoryUrl = "http://repo.spring.io/snapshot";
        for (int i = 0; i < 3; i++)
        {
            Client client = proxyRepositoryConnectionPoolConfigurationService.getClient();
            Response response = client.target(repositoryUrl).request().get();
            response.close();
            client.close();
        }

        // all connections should be released
        assertEquals(0, proxyRepositoryConnectionPoolConfigurationService.getTotalStats().getLeased());
    }

    // integration test, external call to repo
    @Test
    public void connectionsLeakedTest()
    {
        String repositoryUrl = "http://repo.spring.io/snapshot";
        for (int i = 0; i < 3; i++)
        {
            proxyRepositoryConnectionPoolConfigurationService.getClient().target(repositoryUrl).request().get();
        }

        // all connections should be leaked
        assertEquals(3, proxyRepositoryConnectionPoolConfigurationService.getTotalStats().getLeased());
    }
}
