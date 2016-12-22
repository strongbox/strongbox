package org.carlspring.strongbox.cron;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.cron.api.jobs.DownloadRemoteIndexCronJob;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexerFactory;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import javax.inject.Inject;
import java.io.File;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Kate Novik.
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class DownloadRemoteIndexCronJobTest
        extends TestCaseWithArtifactGeneration
{

    private final String storageId = "storage-common-proxies";

    private final String repositoryId = "carlspring";

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ArtifactSearchService artifactSearchService;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Autowired
    private RepositoryIndexerFactory repositoryIndexerFactory;

    @Autowired
    private ConfigurationManager configurationManager;

    @Inject
    private JobManager jobManager;

    public void addRebuildCronJobConfig(String name)
            throws Exception
    {
        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", DownloadRemoteIndexCronJob.class.getName());
        cronTaskConfiguration.addProperty("cronExpression", "0 0/15 * 1/1 * ? *");
        cronTaskConfiguration.addProperty("storageId", storageId);
        cronTaskConfiguration.addProperty("repositoryId", repositoryId);

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

    /**
     * Note: This test requires access to the Internet.
     *
     * @throws Exception
     */
    @Test
    public void testDownloadRemoteRepositoryIndex()
            throws Exception
    {
        //Add repositoryIndex for proxy repository "/storages/storage-common-proxies/carlspring"
        //because this repository was created by strongbox.xml

        System.out.println("Index === " + repositoryIndexManager.getRepositoryIndex(storageId.concat(":")
                                                                                             .concat(repositoryId)));

        if (repositoryIndexManager.getRepositoryIndex(
                storageId.concat(":")
                         .concat(repositoryId)) == null)
        {
            Storage storage = configurationManager.getConfiguration()
                                                  .getStorage(storageId);

            final String storageBasedirPath = storage.getBasedir();
            final File repositoryBasedir = new File(storageBasedirPath, repositoryId);

            final File indexDir = new File(repositoryBasedir, ".index");

            RepositoryIndexer repositoryIndexer = repositoryIndexerFactory.createRepositoryIndexer(storageId,
                                                                                                   repositoryId,
                                                                                                   repositoryBasedir,
                                                                                                   indexDir);

            repositoryIndexManager.addRepositoryIndex(storageId + ":" + repositoryId, repositoryIndexer);
        }

        String jobName = "DownloadIndex";

        addRebuildCronJobConfig(jobName);

        //Checking if job was executed
        while (!jobManager.getExecutedJobs()
                          .containsKey(jobName))
        {
            Thread.sleep(8000);
        }

        System.out.println(jobManager.getExecutedJobs()
                                     .toString());

        SearchRequest request = new SearchRequest(storageId,
                                                  repositoryId,
                                                  "+g:org.carlspring.maven +a:artifact-downloader +v:1.0-SNAPSHOT +p:jar");

        assertTrue(artifactSearchService.contains(request));

        deleteRebuildCronJobConfig(jobName);
    }
}
