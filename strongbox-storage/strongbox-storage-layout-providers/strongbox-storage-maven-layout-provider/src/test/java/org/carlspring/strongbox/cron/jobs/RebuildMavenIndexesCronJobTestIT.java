package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ArtifactSearchService;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author Kate Novik.
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RebuildMavenIndexesCronJobTestIT
        extends TestCaseWithMavenArtifactGenerationAndIndexing
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
        generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-two:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_2.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");

        generateArtifact(REPOSITORY_RELEASES_BASEDIR_3.getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2));
        repositories.add(createRepositoryMock(STORAGE1, REPOSITORY_RELEASES_1));

        return repositories;
    }

    public void addRebuildCronJobConfig(String name,
                                        String storageId,
                                        String repositoryId,
                                        String basePath)
            throws Exception
    {
        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", RebuildMavenIndexesCronJob.class.getName());
        cronTaskConfiguration.addProperty("cronExpression", "0 0/1 * 1/1 * ? *");
        cronTaskConfiguration.addProperty("storageId", storageId);
        cronTaskConfiguration.addProperty("repositoryId", repositoryId);
        cronTaskConfiguration.addProperty("basePath", basePath);

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);
        CronTaskConfiguration obj = cronTaskConfigurationService.findOne(name);
        assertNotNull(obj);
    }

    public void deleteRebuildCronJobConfig(String name)
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
                    deleteRebuildCronJobConfig(jobName);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addRebuildCronJobConfig(jobName, STORAGE0, REPOSITORY_RELEASES_1, ARTIFACT_BASE_PATH_STRONGBOX_INDEXES);
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

                    deleteRebuildCronJobConfig(jobName);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addRebuildCronJobConfig(jobName, STORAGE0, REPOSITORY_RELEASES_1, null);
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

                    deleteRebuildCronJobConfig(jobName);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addRebuildCronJobConfig(jobName, STORAGE0, null, null);
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

                    deleteRebuildCronJobConfig(jobName);
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

        addRebuildCronJobConfig(jobName, null, null, null);
    }

}
