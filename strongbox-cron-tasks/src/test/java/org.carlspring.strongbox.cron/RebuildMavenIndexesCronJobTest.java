package org.carlspring.strongbox.cron;

import org.carlspring.strongbox.cron.api.jobs.RebuildMavenIndexesCronJob;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import javax.inject.Inject;
import java.io.File;
import java.util.List;


import org.apache.maven.artifact.Artifact;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Kate Novik.
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class RebuildMavenIndexesCronJobTest
        extends TestCaseWithArtifactGeneration
{

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private ArtifactSearchService artifactSearchService;

    @Inject
    private JobManager jobManager;

    private static final File REPOSITORY_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage0/releases-one");

    private static final File REPOSITORY_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage0/releases-two");

    private static final File REPOSITORY_BASEDIR_3 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage1/releases");

    private static final String[] CLASSIFIERS = { "javadoc",
                                                  "sources",
                                                  "source-release" };

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_INDEXES = "org/carlspring/strongbox/indexes/strongbox-test-one";

    private static Artifact artifact1;

    private static Artifact artifact2;

    private static Artifact artifact3;

    private static Artifact artifact4;

    private static boolean initialized;

    @Before
    public void setUp()
            throws Exception
    {
        if (!initialized)
        {
            Repository repository1 = new Repository("releases-one");
            repository1.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
            repository1.setIndexingEnabled(true);
            Storage storage = configurationManagementService.getStorage("storage0");
            repository1.setStorage(storage);
            storage.addOrUpdateRepository(repository1);
            repositoryManagementService.createRepository("storage0", "releases-one");

            //Create released artifact
            String ga1 = "org.carlspring.strongbox.indexes:strongbox-test-one";
            artifact1 = generateArtifact(REPOSITORY_BASEDIR_1.getAbsolutePath(), ga1 + ":1.0:jar");

            //Create released artifact
            String ga2 = "org.carlspring.strongbox.indexes:strongbox-test-two";
            artifact2 = generateArtifact(REPOSITORY_BASEDIR_1.getAbsolutePath(), ga2 + ":1.0:jar");

            Repository repository2 = new Repository("releases-two");
            repository2.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
            repository2.setIndexingEnabled(true);
            repository2.setStorage(storage);
            storage.addOrUpdateRepository(repository2);
            repositoryManagementService.createRepository("storage0", "releases-two");

            //Create released artifact
            artifact3 = generateArtifact(REPOSITORY_BASEDIR_2.getAbsolutePath(), ga1 + ":1.0:jar");

            //Create storage and repository for testing rebuild metadata in storages
            Storage newStorage = new Storage("storage1");
            Repository repository3 = new Repository("releases");
            repository3.setPolicy(RepositoryPolicyEnum.RELEASE.getPolicy());
            repository3.setIndexingEnabled(true);
            repository3.setStorage(newStorage);
            configurationManagementService.addOrUpdateStorage(newStorage);
            newStorage.addOrUpdateRepository(repository3);
            repositoryManagementService.createRepository("storage1", "releases");

            //Create released artifact
            artifact4 = generateArtifact(REPOSITORY_BASEDIR_3.getAbsolutePath(), ga1 + ":1.0:jar");

            changeCreationDate(artifact1);
            changeCreationDate(artifact2);
            changeCreationDate(artifact3);
            changeCreationDate(artifact4);

            initialized = true;
        }
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

        addRebuildCronJobConfig(jobName, "storage0", "releases-one", ARTIFACT_BASE_PATH_STRONGBOX_INDEXES);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs().containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        System.out.println(jobManager.getExecutedJobs().toString());

        SearchRequest request = new SearchRequest("storage0",
                                                  "releases-one",
                                                  "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request));

        deleteRebuildCronJobConfig(jobName);
    }

    @Test
    public void testRebuildIndexesInRepository()
            throws Exception
    {
        String jobName = "RebuildIndex-2";

        addRebuildCronJobConfig(jobName, "storage0", "releases-one", null);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs().containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        SearchRequest request1 = new SearchRequest("storage0",
                                                   "releases-one",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest("storage0",
                                                   "releases-one",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-two +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));

        deleteRebuildCronJobConfig(jobName);
    }

    @Test
    public void testRebuildIndexesInStorage()
            throws Exception
    {
        String jobName = "RebuildIndex-3";

        addRebuildCronJobConfig(jobName, "storage0", null, null);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs().containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        SearchRequest request1 = new SearchRequest("storage0",
                                                   "releases-one",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-two +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest("storage0",
                                                   "releases-two",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));

        deleteRebuildCronJobConfig(jobName);
    }

    @Test
    public void testRebuildIndexesInStorages()
            throws Exception
    {
        String jobName = "RebuildIndex-4";

        addRebuildCronJobConfig(jobName, null, null, null);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs().containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        SearchRequest request1 = new SearchRequest("storage0",
                                                   "releases-one",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest("storage1",
                                                   "releases",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));

        deleteRebuildCronJobConfig(jobName);
    }
}
