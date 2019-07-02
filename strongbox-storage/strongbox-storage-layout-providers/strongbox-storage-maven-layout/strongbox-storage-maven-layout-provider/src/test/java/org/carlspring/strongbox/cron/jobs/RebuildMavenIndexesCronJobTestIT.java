package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Kate Novik.
 * @author Pablo Tirado
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
@Execution(CONCURRENT)
public class RebuildMavenIndexesCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String REPOSITORY_RELEASES_1 = "rmicj-releases";
    
    private static final String ARTIFACT_BASE_PATH_STRONGBOX_INDEXES = "org/carlspring/strongbox/indexes/strongbox-test-one";

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
    public void testRebuildArtifactsIndexes(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                             setup = MavenIndexedRepositorySetup.class)
                                            Repository repository,
                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                               id = GROUP_ID + ":" + ARTIFACT_ID1,
                                                               versions = { VERSION })
                                            Path artifact1,
                                            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                               id = GROUP_ID + ":" + ARTIFACT_ID2,
                                                               versions = { VERSION })
                                            Path artifact2)
            throws Exception
    {
        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        // Checking if job was executed
        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
            {
                String query = String.format("+g:%s +a:%s +v:%s +p:jar", GROUP_ID, ARTIFACT_ID1, VERSION);
                SearchRequest request = new SearchRequest(STORAGE0,
                                                          repository.getId(),
                                                          query,
                                                          MavenIndexerSearchProvider.ALIAS);

                try
                {
                    assertTrue(artifactSearchService.contains(request));
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobKey,
                         jobName,
                         RebuildMavenIndexesCronJob.class,
                         STORAGE0,
                         repository.getId(),
                         properties -> properties.put("basePath", ARTIFACT_BASE_PATH_STRONGBOX_INDEXES));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testRebuildIndexesInRepository(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                                setup = MavenIndexedRepositorySetup.class)
                                               Repository repository,
                                               @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                                  id = GROUP_ID + ":" + ARTIFACT_ID1,
                                                                  versions = { VERSION })
                                               Path artifact1,
                                               @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                                  id = GROUP_ID + ":" + ARTIFACT_ID2,
                                                                  versions = { VERSION })
                                               Path artifact2)
            throws Exception
    {
        final UUID jobKey = expectedJobKey;
        final String jobName = expectedJobName;

        jobManager.registerExecutionListener(jobKey.toString(), (jobKey1, statusExecuted) ->
        {
            if (StringUtils.equals(jobKey1, jobKey.toString()) && statusExecuted)
            {
                try
                {
                    String query1 = String.format("+g:%s +a:%s +v:%s +p:jar", GROUP_ID, ARTIFACT_ID1, VERSION);
                    SearchRequest request1 = new SearchRequest(STORAGE0,
                                                               repository.getId(),
                                                               query1,
                                                               MavenIndexerSearchProvider.ALIAS);

                    assertTrue(artifactSearchService.contains(request1));

                    String query2 = String.format("+g:%s +a:%s +v:%s +p:jar", GROUP_ID, ARTIFACT_ID2, VERSION);
                    SearchRequest request2 = new SearchRequest(STORAGE0,
                                                               repository.getId(),
                                                               query2,
                                                               MavenIndexerSearchProvider.ALIAS);

                    assertTrue(artifactSearchService.contains(request2));
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobKey,
                         jobName,
                         RebuildMavenIndexesCronJob.class,
                         STORAGE0,
                         repository.getId());

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

}
