package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author Martin Todorov
 */
@CronTaskTest
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class DownloadRemoteMavenIndexCronJobTestIT
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "drmicj-releases";

    private static final String REPOSITORY_PROXIED_RELEASES = "drmicj-proxied-releases";

    private static final File REPOSITORY_RELEASES_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/" + STORAGE0 + "/" +
                                                                     REPOSITORY_RELEASES);

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

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
        createRepository(STORAGE0, REPOSITORY_RELEASES, true);

        createProxyRepository(STORAGE0,
                              REPOSITORY_PROXIED_RELEASES,
                              "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_PROXIED_RELEASES);

        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes.download.remote:strongbox-test-one:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes.download.remote:strongbox-test-two:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes.download.remote:strongbox-test-three:1.0:jar");
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_PROXIED_RELEASES));

        return repositories;
    }

    public void addCronJobConfig(String name,
                                 String storageId,
                                 String repositoryId)
            throws Exception
    {
        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", DownloadRemoteMavenIndexCronJob.class.getName());
        cronTaskConfiguration.addProperty("cronExpression", "0 0/1 * 1/1 * ? *");
        cronTaskConfiguration.addProperty("storageId", storageId);
        cronTaskConfiguration.addProperty("repositoryId", repositoryId);

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);

        CronTaskConfiguration configuration = cronTaskConfigurationService.findOne(name);

        assertNotNull(configuration);
    }

    public void deleteCronJobConfig(String name)
            throws Exception
    {
        List<CronTaskConfiguration> confs = cronTaskConfigurationService.getConfiguration(name);

        for (CronTaskConfiguration cnf : confs)
        {
            assertNotNull(cnf);

            cronTaskConfigurationService.deleteConfiguration(cnf);
        }

        assertNull(cronTaskConfigurationService.findOne(name));
    }

    @Test
    public void testRebuildArtifactsIndexes()
            throws Exception
    {
        String jobName = "Download remote indexes for " + STORAGE0 + ":" + "drmicj-releases";

        // Checking if job was executed
        jobManager.registerExecutionListener(jobName, (jobName1, statusExecuted) ->
        {
            if (jobName1.equals(jobName) && statusExecuted)
            {
                SearchRequest request1 = new SearchRequest(STORAGE0,
                                                           REPOSITORY_RELEASES,
                                                           "+g:org.carlspring.strongbox.indexes " +
                                                           "+a:strongbox-test-one " +
                                                           "+v:1.0 " +
                                                           "+p:jar");

                SearchRequest request2 = new SearchRequest(STORAGE0,
                                                           REPOSITORY_PROXIED_RELEASES,
                                                           "+g:org.carlspring.strongbox.indexes " +
                                                           "+a:strongbox-test-one " +
                                                           "+v:1.0 " +
                                                           "+p:jar");
                request2.addOption("indexType", IndexTypeEnum.REMOTE.getType());

                try
                {
                    assertTrue(artifactSearchService.contains(request1));
                    assertTrue(artifactSearchService.contains(request2));

                    deleteCronJobConfig(jobName);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addCronJobConfig(jobName, STORAGE0, REPOSITORY_RELEASES);
    }

}
