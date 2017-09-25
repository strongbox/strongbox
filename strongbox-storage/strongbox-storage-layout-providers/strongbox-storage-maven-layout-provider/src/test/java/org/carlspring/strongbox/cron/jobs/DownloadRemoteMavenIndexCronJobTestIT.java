package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.event.cron.CronTaskEventTypeEnum;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;

import javax.inject.Inject;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Todorov
 */
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DownloadRemoteMavenIndexCronJobTestIT
        extends BaseCronJobWithMavenIndexingTestCase
{

    private static final String REPOSITORY_RELEASES = "drmicj-releases";

    private static final String REPOSITORY_PROXIED_RELEASES = "drmicj-proxied-releases";

    private static final File REPOSITORY_RELEASES_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/" + STORAGE0 + "/" +
                                                                     REPOSITORY_RELEASES);

    @Inject
    private ArtifactSearchService artifactSearchService;

    @Inject
    private JobManager jobManager;

    @Inject
    private CronJobSchedulerService cronJobSchedulerService;


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
        // Register to receive cron task-related events
        cronTaskEventListenerRegistry.addListener(this);

        createRepository(STORAGE0, REPOSITORY_RELEASES, true);

        createProxyRepository(STORAGE0,
                              REPOSITORY_PROXIED_RELEASES,
                              "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes.download:strongbox-test-one:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes.download:strongbox-test-two:1.0:jar");

        reIndex(STORAGE0, REPOSITORY_RELEASES, "/");
        reIndex(STORAGE0, REPOSITORY_PROXIED_RELEASES, "/");

        packIndex(STORAGE0, REPOSITORY_RELEASES);

        // Requests against the local index of the hosted repository from which we're later on proxying:
        // org.carlspring.strongbox.indexes.download:strongbox-test-one:1.0:jar
        SearchRequest request1 = new SearchRequest(STORAGE0,
                                                   REPOSITORY_RELEASES,
                                                   "+g:org.carlspring.strongbox.indexes.download " +
                                                   "+a:strongbox-test-one " +
                                                   "+v:1.0 " +
                                                   "+p:jar",
                                                   MavenIndexerSearchProvider.ALIAS);
        // org.carlspring.strongbox.indexes.download:strongbox-test-two:1.0:jar
        SearchRequest request2 = new SearchRequest(STORAGE0,
                                                   REPOSITORY_RELEASES,
                                                   "+g:org.carlspring.strongbox.indexes.download " +
                                                   "+a:strongbox-test-two " +
                                                   "+v:1.0 " +
                                                   "+p:jar",
                                                   MavenIndexerSearchProvider.ALIAS);

        // Check that the artifacts exist in the hosted repository's local index
        assertTrue("Failed to find any results for " + request1.getQuery() + " in the hosted repository!",
                   artifactSearchService.contains(request1));

        System.out.println(request1.getQuery() + " found matches!");

        assertTrue("Failed to find any results for " + request2.getQuery() + " in the hosted repository!",
                   artifactSearchService.contains(request2));

        System.out.println(request2.getQuery() + " found matches!");
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_PROXIED_RELEASES));

        return repositories;
    }

    public CronTaskConfiguration addCronJobConfig(String name,
                                                  String storageId,
                                                  String repositoryId)
            throws Exception
    {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("cronExpression", "0 11 11 11 11 ? 2100");
        properties.put("storageId", storageId);
        properties.put("repositoryId", repositoryId);

        return addCronJobConfig(name, DownloadRemoteMavenIndexCronJob.class.getName(), properties);
    }

    @Test
    public void testDownloadRemoteIndexAndExecuteSearch()
            throws Exception
    {
        String jobName = "Download remote indexes for " + STORAGE0 + ":" + "drmicj-releases";

        // Checking if job was executed
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                // Requests against the remote index on the proxied repository:
                // org.carlspring.strongbox.indexes.download:strongbox-test-one:1.0:jar
                SearchRequest request3 = new SearchRequest(STORAGE0,
                                                           REPOSITORY_PROXIED_RELEASES,
                                                           "+g:org.carlspring.strongbox.indexes.download " +
                                                           "+a:strongbox-test-one " +
                                                           "+v:1.0 " +
                                                           "+p:jar",
                                                           MavenIndexerSearchProvider.ALIAS);
                request3.addOption("indexType", IndexTypeEnum.REMOTE.getType());

                // org.carlspring.strongbox.indexes.download:strongbox-test-two:1.0:jar
                SearchRequest request4 = new SearchRequest(STORAGE0,
                                                           REPOSITORY_PROXIED_RELEASES,
                                                           "+g:org.carlspring.strongbox.indexes.download " +
                                                           "+a:strongbox-test-two " +
                                                           "+v:1.0 " +
                                                           "+p:jar",
                                                           MavenIndexerSearchProvider.ALIAS);
                request4.addOption("indexType", IndexTypeEnum.REMOTE.getType());

                try
                {
                    // Check that the artifacts exist in the proxied repository's remote index
                    assertTrue("Failed to find any results for " + request3.getQuery() + " in the remote index!",
                               artifactSearchService.contains(request3));

                    System.out.println(request3.getQuery() + " found matches!");

                    assertTrue("Failed to find any results for " + request4.getQuery() + " in the remote index!",
                               artifactSearchService.contains(request4));

                    System.out.println(request4.getQuery() + " found matches!");
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        CronTaskConfiguration configuration = addCronJobConfig(jobName, STORAGE0, REPOSITORY_PROXIED_RELEASES);

        cronJobSchedulerService.executeJob(configuration);

        assertTrue("Failed to execute task!", expectEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType()));
    }

}
