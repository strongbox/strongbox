package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;

import javax.inject.Inject;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Martin Todorov
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
@Execution(CONCURRENT)
public class DownloadRemoteMavenIndexCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String REPOSITORY_RELEASES = "drmicj-releases";

    private static final String REPOSITORY_PROXIED_RELEASES = "drmicj-proxied-releases";

    private static final String PROXY_REPOSITORY_URL = "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES + "/";

    private static final String GROUP_ID = "org.carlspring.strongbox.indexes.download";

    private static final String ARTIFACT_ID1 = "strongbox-test-one";

    private static final String ARTIFACT_ID2 = "strongbox-test-two";

    private static final String VERSION = "1.0";


    @Inject
    private ArtifactSearchService artifactSearchService;

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testDownloadRemoteIndexAndExecuteSearch(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES, setup = MavenIndexedRepositorySetup.class)
                    Repository repository,
            @Remote(url = PROXY_REPOSITORY_URL)
            @MavenRepository(repositoryId = REPOSITORY_PROXIED_RELEASES, setup = MavenIndexedRepositorySetup.class)
                    Repository proxyRepository,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES, id = GROUP_ID + ":" +
                                                                        ARTIFACT_ID1, versions = { VERSION })
                    Path artifact1,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES, id = GROUP_ID + ":" +
                                                                        ARTIFACT_ID2, versions = { VERSION })
                    Path artifact2)
            throws Exception
    {
        IndexedMavenRepositoryFeatures features = (IndexedMavenRepositoryFeatures) getFeatures();

        features.reIndex(STORAGE0, repository.getId(), "/");
        features.reIndex(STORAGE0, proxyRepository.getId(), "/");

        // Make sure the repository that is being proxied has a packed index to serve:
        features.pack(STORAGE0, repository.getId());

        // Requests against the local index of the hosted repository from which we're later on proxying:
        // org.carlspring.strongbox.indexes.download:strongbox-test-one:1.0:jar
        String query1 = String.format("+g:%s +a:%s +v:%s +p:jar", GROUP_ID, ARTIFACT_ID1, VERSION);
        SearchRequest request1 = new SearchRequest(STORAGE0,
                                                   repository.getId(),
                                                   query1,
                                                   MavenIndexerSearchProvider.ALIAS);

        // Check that the artifacts exist in the hosted repository's local index
        assertTrue(artifactSearchService.contains(request1),
                   "Failed to find any results for " + request1.getQuery() + " in the hosted repository!");

        System.out.println(request1.getQuery() + " found matches!");

        // org.carlspring.strongbox.indexes.download:strongbox-test-two:1.0:jar
        String query2 = String.format("+g:%s +a:%s +v:%s +p:jar", GROUP_ID, ARTIFACT_ID2, VERSION);
        SearchRequest request2 = new SearchRequest(STORAGE0,
                                                   repository.getId(),
                                                   query2,
                                                   MavenIndexerSearchProvider.ALIAS);

        assertTrue(artifactSearchService.contains(request2),
                   "Failed to find any results for " + request2.getQuery() + " in the hosted repository!");

        System.out.println(request2.getQuery() + " found matches!");

        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        // Checking if job was executed
        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
            {
                // Requests against the remote index on the proxied repository:
                // org.carlspring.strongbox.indexes.download:strongbox-test-one:1.0:jar
                SearchRequest request3 = new SearchRequest(STORAGE0,
                                                           proxyRepository.getId(),
                                                           query1,
                                                           MavenIndexerSearchProvider.ALIAS);
                request3.addOption("indexType", IndexTypeEnum.REMOTE.getType());

                // org.carlspring.strongbox.indexes.download:strongbox-test-two:1.0:jar
                SearchRequest request4 = new SearchRequest(STORAGE0,
                                                           proxyRepository.getId(),
                                                           query2,
                                                           MavenIndexerSearchProvider.ALIAS);
                request4.addOption("indexType", IndexTypeEnum.REMOTE.getType());

                try
                {
                    // Check that the artifacts exist in the proxied repository's remote index
                    assertTrue(artifactSearchService.contains(request3),
                               "Failed to find any results for " + request3.getQuery() + " in the remote index!");

                    System.out.println(request3.getQuery() + " found matches!");

                    assertTrue(artifactSearchService.contains(request4),
                               "Failed to find any results for " + request4.getQuery() + " in the remote index!");

                    System.out.println(request4.getQuery() + " found matches!");
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobKey, jobName, DownloadRemoteMavenIndexCronJobTestSubj.class, STORAGE0,
                         proxyRepository.getId());

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    public static class DownloadRemoteMavenIndexCronJobTestSubj
            extends DownloadRemoteMavenIndexCronJob
    {

        @Override
        public boolean enabled(CronTaskConfigurationDto configuration,
                               Environment env)
        {
            return true;
        }

    }

}
