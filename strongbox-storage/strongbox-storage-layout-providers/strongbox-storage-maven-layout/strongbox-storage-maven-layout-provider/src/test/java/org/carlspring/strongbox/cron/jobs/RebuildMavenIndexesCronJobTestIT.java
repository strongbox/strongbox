package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.search.SearchRequest;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
@Execution(CONCURRENT)
public class RebuildMavenIndexesCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    @Inject
    private ConfigurationResourceResolver configurationResourceResolver;

    private static final String REPOSITORY_RELEASES_1 = "rmicj-releases";

    private final File REPOSITORY_RELEASES_BASEDIR_1 = new File(configurationResourceResolver.getVaultDirectory() +
                                                                "/storages/" + STORAGE0 + "/" +
                                                                REPOSITORY_RELEASES_1);

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_INDEXES = "org/carlspring/strongbox/indexes/strongbox-test-one";

    @Inject
    private ArtifactSearchService artifactSearchService;

    private Set<MutableRepository> getRepositories(TestInfo testInfo)
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0,
                                              getRepositoryName(REPOSITORY_RELEASES_1, testInfo),
                                              Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);

        createRepository(STORAGE0,
                         getRepositoryName(REPOSITORY_RELEASES_1, testInfo),
                         true);

        generateArtifact(getRepositoryBasedir(REPOSITORY_RELEASES_BASEDIR_1, testInfo),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");

        generateArtifact(getRepositoryBasedir(REPOSITORY_RELEASES_BASEDIR_1, testInfo),
                         "org.carlspring.strongbox.indexes:strongbox-test-two:1.0:jar");
    }

    @AfterEach
    public void removeRepositories(TestInfo testInfo)
            throws Exception
    {
        removeRepositories(getRepositories(testInfo));
    }

    @Test
    public void testRebuildArtifactsIndexes(TestInfo testInfo)
            throws Exception
    {
        final String jobName = expectedJobName;

        // Checking if job was executed
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                SearchRequest request = new SearchRequest(STORAGE0,
                                                          getRepositoryName(REPOSITORY_RELEASES_1, testInfo),
                                                          "+g:org.carlspring.strongbox.indexes " +
                                                          "+a:strongbox-test-one " +
                                                          "+v:1.0 " +
                                                          "+p:jar",
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

        addCronJobConfig(jobName,
                         RebuildMavenIndexesCronJob.class,
                         STORAGE0,
                         getRepositoryName(REPOSITORY_RELEASES_1, testInfo),
                         properties -> properties.put("basePath", ARTIFACT_BASE_PATH_STRONGBOX_INDEXES));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    public void testRebuildIndexesInRepository(TestInfo testInfo)
            throws Exception
    {
        final String jobName = expectedJobName;
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    SearchRequest request1 = new SearchRequest(STORAGE0,
                                                               getRepositoryName(REPOSITORY_RELEASES_1, testInfo),
                                                               "+g:org.carlspring.strongbox.indexes " +
                                                               "+a:strongbox-test-one " +
                                                               "+v:1.0 " +
                                                               "+p:jar",
                                                               MavenIndexerSearchProvider.ALIAS);

                    assertTrue(artifactSearchService.contains(request1));

                    SearchRequest request2 = new SearchRequest(STORAGE0,
                                                               getRepositoryName(REPOSITORY_RELEASES_1, testInfo),
                                                               "+g:org.carlspring.strongbox.indexes " +
                                                               "+a:strongbox-test-two " +
                                                               "+v:1.0 " +
                                                               "+p:jar",
                                                               MavenIndexerSearchProvider.ALIAS);

                    assertTrue(artifactSearchService.contains(request2));
                }
                catch (Exception e)
                {
                    throw new UndeclaredThrowableException(e);
                }
            }
        });

        addCronJobConfig(jobName,
                         RebuildMavenIndexesCronJob.class,
                         STORAGE0,
                         getRepositoryName(REPOSITORY_RELEASES_1, testInfo));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

}
