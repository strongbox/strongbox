package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.search.SearchRequest;

import javax.inject.Inject;
import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class }, mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
public class RebuildMavenIndexesCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String STORAGE1 = "storage1";

    private static final String REPOSITORY_RELEASES_1 = "rmicj-releases";

    private static final String REPOSITORY_RELEASES_2 = "rmicj-releases-test";

    private static final File REPOSITORY_RELEASES_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE0 + "/" +
                                                                       REPOSITORY_RELEASES_1);

    private static final File REPOSITORY_RELEASES_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE0 + "/" +
                                                                       REPOSITORY_RELEASES_2);

    private static final File REPOSITORY_RELEASES_BASEDIR_3 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                       "/storages/" + STORAGE1 + "/" +
                                                                       REPOSITORY_RELEASES_1);

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_INDEXES = "org/carlspring/strongbox/indexes/strongbox-test-one";

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private ArtifactSearchService artifactSearchService;

    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_RELEASES_1, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Override
    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {
        super.init(testInfo);

        createStorage(STORAGE1);

        createRepository(STORAGE0, REPOSITORY_RELEASES_1, true);
        createRepository(STORAGE0, REPOSITORY_RELEASES_2, true);
        createRepository(STORAGE1, REPOSITORY_RELEASES_1, true);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-two:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_2.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_3.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");
    }

    @AfterEach
    public void removeRepositories()
            throws Exception
    {
        closeIndexersForRepository(STORAGE1, REPOSITORY_RELEASES_1);
        closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES_2);
        closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES_1);
        removeRepositories(getRepositoriesToClean());
        cleanUp();
    }

    @Test
    public void testRebuildArtifactsIndexes()
            throws Exception
    {
        final String jobName = expectedJobName;

        // Checking if job was executed
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                SearchRequest request = new SearchRequest(STORAGE0, REPOSITORY_RELEASES_1,
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

        addCronJobConfig(jobName, RebuildMavenIndexesCronJob.class, STORAGE0, REPOSITORY_RELEASES_1,
                         properties -> properties.put("basePath", ARTIFACT_BASE_PATH_STRONGBOX_INDEXES));

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

    @Test
    public void testRebuildIndexesInRepository()
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
                                                               REPOSITORY_RELEASES_1,
                                                               "+g:org.carlspring.strongbox.indexes " +
                                                               "+a:strongbox-test-one " +
                                                               "+v:1.0 " +
                                                               "+p:jar",
                                                               MavenIndexerSearchProvider.ALIAS);

                    assertTrue(artifactSearchService.contains(request1));

                    SearchRequest request2 = new SearchRequest(STORAGE0,
                                                               REPOSITORY_RELEASES_1,
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

        addCronJobConfig(jobName, RebuildMavenIndexesCronJob.class, STORAGE0, REPOSITORY_RELEASES_1);

        await().atMost(EVENT_TIMEOUT_SECONDS, TimeUnit.SECONDS).untilTrue(receivedExpectedEvent());
    }

}
