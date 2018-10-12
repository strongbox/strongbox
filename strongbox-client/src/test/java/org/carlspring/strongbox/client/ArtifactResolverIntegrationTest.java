package org.carlspring.strongbox.client;

import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author korest
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
public class ArtifactResolverIntegrationTest
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactResolverIntegrationTest.class);

    @Configuration
    @Import({ClientConfig.class})
    public static class SpringConfig
    {
    }

    private RestArtifactResolver artifactResolver;

    // fake url
    private String repositoryUrl = "https://repo.maven.apache.org/maven2/";

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @BeforeEach
    public void setUp()
    {
        artifactResolver = new RestArtifactResolver(proxyRepositoryConnectionPoolConfigurationService.getRestClient(),
                                                    repositoryUrl,
                                                    new RemoteRepositoryRetryArtifactDownloadConfiguration(MutableRemoteRepositoryRetryArtifactDownloadConfiguration.DEFAULT));
    }

    @Test
    public void allConnectionsReleasedTest() throws InterruptedException
    {
        MultiHttpClientConnThread[] threads = new MultiHttpClientConnThread[10];
        for (int i = 0; i < threads.length; i++)
        {
            threads[i] = new MultiHttpClientConnThread(artifactResolver, repositoryUrl);
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

        private RestArtifactResolver artifactResolver;

        private String url;

        public MultiHttpClientConnThread(RestArtifactResolver artifactResolver,
                                         String url)
        {
            this.artifactResolver = artifactResolver;
            this.url = url;
        }

        @Override
        public final void run()
        {
            CloseableRestResponse response = artifactResolver.get(url);
            ResourceCloser.close(response, LOGGER);
        }

    }
}
