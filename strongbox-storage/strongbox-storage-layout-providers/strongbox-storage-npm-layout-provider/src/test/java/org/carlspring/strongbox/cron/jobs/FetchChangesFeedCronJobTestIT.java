package org.carlspring.strongbox.cron.jobs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;

import org.carlspring.strongbox.config.NpmLayoutProviderTestConfig;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.repositories.ArtifactRepository;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryData;
import org.carlspring.strongbox.testing.NpmReplicateUrlRepositorySetup;
import org.carlspring.strongbox.testing.repository.NpmRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;
import org.carlspring.strongbox.yaml.configuration.repository.remote.NpmRemoteRepositoryConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = { NpmLayoutProviderTestConfig.class })
@ActiveProfiles(profiles = { "test", "FetchChangesFeedCronJobTestConfig" })
@SpringBootTest
@Execution(CONCURRENT)
public class FetchChangesFeedCronJobTestIT
        extends BaseCronJobWithNpmIndexingTestCase
{

    private static final String EMPTY_FEED = "{\"results\": [],\"last_seq\": 322}";

    private static final String STORAGE = "storage-npm-fcfcjt";

    private static final String REPOSITORY_RELEASES = "fcfcjt-releases";

    private static final String REMOTE_URL = "https://registry.npmjs.org";

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;
    
    @Inject
    private ArtifactRepository artifactRepository;

    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);
        prepareArtifactResolverContext(this.getClass().getResourceAsStream("changesFeed.json"));
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testFetchChangesFeed(@Remote(url = REMOTE_URL)
                                     @NpmRepository(storageId = STORAGE,
                                                    repositoryId = REPOSITORY_RELEASES,
                                                    setup = NpmReplicateUrlRepositorySetup.class)
                                     Repository repository)
        throws Exception
    {
        addCronJobConfig(expectedJobKey,
                         expectedJobName,
                         TestFetchRemoteChangesFeedCronJob.class,
                         repository.getStorage().getId(),
                         repository.getId());

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());

        List<Artifact> artifactEntryList = artifactRepository.findByPathLike(repository.getStorage().getId(), repository.getId(),
                                                                             "MiniMVC");
        assertThat(artifactEntryList).hasSize(1);

        Artifact artifactEntry = artifactEntryList.iterator().next();
        assertThat(artifactEntry.getArtifactFileExists()).isFalse();

        RepositoryData repositoryData = (RepositoryData) configurationManagementService.getConfiguration()
                                                                                       .getRepository(
                                                                                               repository.getStorage().getId(),
                                                                                               repository.getId());

        NpmRemoteRepositoryConfiguration customConfiguration = (NpmRemoteRepositoryConfiguration) repositoryData.getRemoteRepository()
                                                                                                                .getCustomConfiguration();
        assertThat(customConfiguration.getLastChangeId()).isEqualTo(330L);
    }

    public static class TestFetchRemoteChangesFeedCronJob extends FetchRemoteNpmChangesFeedCronJob
    {

        @Override
        public boolean enabled(CronTaskConfigurationDto configuration,
                               Environment env)
        {
            return true;
        }

    }

    private void prepareArtifactResolverContext(InputStream feedInputStream)
    {

        Client mockedRestClient = mock(Client.class);

        Invocation mockedInvocation = mock(Invocation.class);
        when(mockedInvocation.invoke(InputStream.class))
                .thenReturn(feedInputStream, new ByteArrayInputStream(EMPTY_FEED.getBytes()));

        Invocation.Builder mockedBuilder = mock(Invocation.Builder.class);
        when(mockedBuilder.buildGet()).thenReturn(mockedInvocation);

        WebTarget mockedWebTarget = mock(WebTarget.class);
        when(mockedWebTarget.path(anyString())).thenReturn(mockedWebTarget);
        when(mockedWebTarget.queryParam(anyString(), any()))
                .thenReturn(mockedWebTarget);

        when(mockedWebTarget.request()).thenReturn(mockedBuilder);

        when(mockedRestClient.target(anyString())).thenReturn(mockedWebTarget);

        when(proxyRepositoryConnectionPoolConfigurationService.getRestClient())
                .thenReturn(mockedRestClient);
    }

    @Profile("FetchChangesFeedCronJobTestConfig")
    @Configuration
    public static class FetchChangesFeedCronJobTestConfig
    {

        @Primary
        @Bean
        public ProxyRepositoryConnectionPoolConfigurationService mockedProxyRepositoryConnectionPoolConfigurationService()
        {
            return mock(ProxyRepositoryConnectionPoolConfigurationService.class);
        }

    }
}
