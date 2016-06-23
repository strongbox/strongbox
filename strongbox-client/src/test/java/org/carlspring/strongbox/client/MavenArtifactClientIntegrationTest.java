package org.carlspring.strongbox.client;

import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.DataServiceConfig;
import org.carlspring.strongbox.data.domain.PoolConfiguration;
import org.carlspring.strongbox.data.service.PoolConfigurationService;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * @author korest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class MavenArtifactClientIntegrationTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenArtifactClientIntegrationTest.class);

    @Configuration
    @Import({DataServiceConfig.class, ClientConfig.class})
    public static class SpringConfig
    {
    }

    private MavenArtifactClient mavenArtifactClient;

    // fake url
    private String repositoryUrl = "https://repo.maven.apache.org/maven2/";

    @Autowired
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @Before
    public void setUp()
    {
        mavenArtifactClient = MavenArtifactClient
                .getTestInstance(proxyRepositoryConnectionPoolConfigurationService.getClient(), repositoryUrl, null,
                        null);
    }

    @Test
    public void allConnectionsReleasedTest() throws InterruptedException
    {
        MultiHttpClientConnThread[] threads = new MultiHttpClientConnThread[10];
        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new MultiHttpClientConnThread(mavenArtifactClient, repositoryUrl);
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++)
        {
            threads[i].join();
        }

        // all connections should be released
        assertEquals(0, proxyRepositoryConnectionPoolConfigurationService.getPoolStats(repositoryUrl).getLeased());
    }

    public static final class MultiHttpClientConnThread extends Thread
    {

        private MavenArtifactClient mavenArtifactClient;

        private String url;

        public MultiHttpClientConnThread(MavenArtifactClient mavenArtifactClient, String url)
        {
            this.mavenArtifactClient = mavenArtifactClient;
            this.url = url;
        }

        @Override
        public final void run()
        {
            try
            {
                mavenArtifactClient.getResource(url);
            }
            catch (ArtifactTransportException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
            catch (IOException e)
            {
                LOGGER.error(e.getMessage(), e);
            }
        }

    }
}
