package org.carlspring.strongbox.cron;

import org.carlspring.strongbox.cron.api.jobs.RebuildMavenIndexesCronJob;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
public class RebuildMavenIndexesCronJobTest
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

    private static boolean INITIALIZED;

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

    @PostConstruct
    public void initialize()
            throws Exception
    {
        if (!INITIALIZED)
        {
            createRepository(STORAGE0, REPOSITORY_RELEASES_1, RepositoryPolicyEnum.RELEASE.getPolicy(), true);

            generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                             "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");

            generateArtifact(REPOSITORY_RELEASES_BASEDIR_1.getAbsolutePath(),
                             "org.carlspring.strongbox.indexes:strongbox-test-two:1.0:jar");

            createRepository(STORAGE0, REPOSITORY_RELEASES_2, RepositoryPolicyEnum.RELEASE.getPolicy(), true);

            generateArtifact(REPOSITORY_RELEASES_BASEDIR_2.getAbsolutePath(),
                             "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");

            createStorage(STORAGE1);

            createRepository(STORAGE1, REPOSITORY_RELEASES_1, RepositoryPolicyEnum.RELEASE.getPolicy(), true);

            generateArtifact(REPOSITORY_RELEASES_BASEDIR_3.getAbsolutePath(),
                             "org.carlspring.strongbox.indexes:strongbox-test-one:1.0:jar");

            INITIALIZED = true;
        }
    }

    @PreDestroy
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
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

        addRebuildCronJobConfig(jobName, STORAGE0, REPOSITORY_RELEASES_1, ARTIFACT_BASE_PATH_STRONGBOX_INDEXES);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs().containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        System.out.println(jobManager.getExecutedJobs().toString());

        SearchRequest request = new SearchRequest(STORAGE0, REPOSITORY_RELEASES_1,
                                                  "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request));

        deleteRebuildCronJobConfig(jobName);
    }

    @Test
    public void testRebuildIndexesInRepository()
            throws Exception
    {
        String jobName = "RebuildIndex-2";

        addRebuildCronJobConfig(jobName, STORAGE0, REPOSITORY_RELEASES_1, null);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs().containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        SearchRequest request1 = new SearchRequest(STORAGE0, REPOSITORY_RELEASES_1,
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest(STORAGE0, REPOSITORY_RELEASES_1,
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-two +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));

        deleteRebuildCronJobConfig(jobName);
    }

    @Test
    public void testRebuildIndexesInStorage()
            throws Exception
    {
        String jobName = "RebuildIndex-3";

        addRebuildCronJobConfig(jobName, STORAGE0, null, null);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs().containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        SearchRequest request1 = new SearchRequest(STORAGE0, REPOSITORY_RELEASES_1,
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-two +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest(STORAGE0, REPOSITORY_RELEASES_2,
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

        SearchRequest request1 = new SearchRequest(STORAGE0, REPOSITORY_RELEASES_2,
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest(STORAGE1, REPOSITORY_RELEASES_1,
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));

        deleteRebuildCronJobConfig(jobName);
    }
}
