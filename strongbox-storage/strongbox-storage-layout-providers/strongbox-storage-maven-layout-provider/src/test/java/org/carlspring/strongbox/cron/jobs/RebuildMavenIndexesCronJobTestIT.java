package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.event.cron.CronTaskEventTypeEnum;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;

import javax.inject.Inject;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author Kate Novik.
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
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

    @Inject
    private JobManager jobManager;

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Before
    public void initialize()
            throws Exception
    {
        createRepository(STORAGE0, REPOSITORY_RELEASES_1, true);
        createRepository(STORAGE0, REPOSITORY_RELEASES_2, true);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-two:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_2.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_3.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");
    }

    @After
    public void removeRepositories()
            throws Exception
    {
        getRepositoryIndexManager().closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES_2);
        getRepositoryIndexManager().closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES_1);
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2));
       // repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_RELEASES_1));

        return repositories;
    }

    @Test
    public void testRebuildArtifactsIndexes()
            throws Exception
    {
        String jobName = "RebuildIndex-1";

        // Checking if job was executed
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                SearchRequest request = new SearchRequest(STORAGE0, REPOSITORY_RELEASES_1,
                                                          "+g:org.carlspring.strongbox.indexes " +
                                                          "+a:strongbox-test-one " +
                                                          "+v:1.0 " +
                                                          "+p:jar");

                try
                {
                    assertTrue(artifactSearchService.contains(request));
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenIndexesCronJob.class, STORAGE0, REPOSITORY_RELEASES_1,
                         properties -> properties.put("basePath", ARTIFACT_BASE_PATH_STRONGBOX_INDEXES));

        assertTrue("Failed to execute task!",
                   expectEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType()));
    }

    @Test
    public void testRebuildIndexesInRepository()
            throws Exception
    {
        String jobName = "RebuildIndex-2";
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
                                                               "+p:jar");

                    assertTrue(artifactSearchService.contains(request1));

                    SearchRequest request2 = new SearchRequest(STORAGE0,
                                                               REPOSITORY_RELEASES_1,
                                                               "+g:org.carlspring.strongbox.indexes " +
                                                               "+a:strongbox-test-two " +
                                                               "+v:1.0 " +
                                                               "+p:jar");

                    assertTrue(artifactSearchService.contains(request2));
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenIndexesCronJob.class, STORAGE0, REPOSITORY_RELEASES_1);

        assertTrue("Failed to execute task!",
                   expectEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType()));
    }

    @Test
    public void testRebuildIndexesInStorage()
            throws Exception
    {
        String jobName = "RebuildIndex-3";
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    SearchRequest request1 = new SearchRequest(STORAGE0,
                                                               REPOSITORY_RELEASES_1,
                                                               "+g:org.carlspring.strongbox.indexes " +
                                                               "+a:strongbox-test-two " +
                                                               "+v:1.0 " +
                                                               "+p:jar");

                    assertTrue(artifactSearchService.contains(request1));

                    SearchRequest request2 = new SearchRequest(STORAGE0,
                                                               REPOSITORY_RELEASES_2,
                                                               "+g:org.carlspring.strongbox.indexes " +
                                                               "+a:strongbox-test-one " +
                                                               "+v:1.0 " +
                                                               "+p:jar");

                    assertTrue(artifactSearchService.contains(request2));
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenIndexesCronJob.class, STORAGE0, null);

        assertTrue("Failed to execute task!",
                   expectEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType()));
    }

    @Test
    public void testRebuildIndexesInStorages()
            throws Exception
    {
        String jobName = "RebuildIndex-4";
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                try
                {
                    SearchRequest request1 = new SearchRequest(STORAGE0,
                                                               REPOSITORY_RELEASES_2,
                                                               "+g:org.carlspring.strongbox.indexes " +
                                                               "+a:strongbox-test-one " +
                                                               "+v:1.0 " +
                                                               "+p:jar");

                    assertTrue(artifactSearchService.contains(request1));

                    SearchRequest request2 = new SearchRequest(STORAGE1,
                                                               REPOSITORY_RELEASES_1,
                                                               "+g:org.carlspring.strongbox.indexes " +
                                                               "+a:strongbox-test-one " +
                                                               "+v:1.0 " +
                                                               "+p:jar");

                    assertTrue(artifactSearchService.contains(request2));
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, RebuildMavenIndexesCronJob.class, null, null);

        assertTrue("Failed to execute task!",
                   expectEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType()));
    }

}
