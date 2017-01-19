package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.booters.StorageBooter;
import org.carlspring.strongbox.cron.api.jobs.DownloadRemoteIndexCronJob;
import org.carlspring.strongbox.cron.config.JobManager;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.CronTaskTest;
import org.carlspring.strongbox.services.ArtifactIndexesService;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexerFactory;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.repository.RemoteRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.jayway.restassured.http.ContentType;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.Assert.*;

/**
 * @author Kate Novik.
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class DownloadRemoteIndexCronJobTest
        extends RestAssuredBaseTest
{

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                            "/storages/storage0/releases");

    private final String storageId = "storage0";

    private final String repositoryId = "carlspring";

    private Repository repository1;

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private ArtifactSearchService artifactSearchService;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private RepositoryIndexerFactory repositoryIndexerFactory;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private ArtifactIndexesService artifactIndexesService;

    @Inject
    private StorageBooter storageBooter;

    @Inject
    private JobManager jobManager;

    public DownloadRemoteIndexCronJobTest()
            throws PlexusContainerException, ComponentLookupException
    {
    }

    public void prepareTest()
            throws Exception
    {
        super.init();
        // Initialize indexes (for IDE launches)
        if (repositoryIndexManager.getIndexes()
                                  .isEmpty())
        {
            storageBooter.reInitializeRepositoryIndex("storage0", "releases");

        }

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC(
                "org.carlspring.strongbox:strongbox-commons:1.0:jar");
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC(
                "org.carlspring.strongbox:strongbox-commons:1.1:jar");
        Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC(
                "org.carlspring.strongbox:strongbox-commons:1.2:jar");

        createArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact1);
        createArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact2);
        createArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact3);


        artifactIndexesService.rebuildIndexes("storage0", "releases", null);

        repository1 = new Repository("carlspring");
        repository1.setPolicy(RepositoryPolicyEnum.MIXED.getPolicy());
        repository1.setType(RepositoryTypeEnum.PROXY.getType());

        RemoteRepository remoteRepository = new RemoteRepository();
        remoteRepository.setDownloadRemoteIndexes(true);
        remoteRepository.setUrl(getContextBaseUrl() + "/storages/storage0/releases/");
        remoteRepository.setAutoBlocking(true);
        remoteRepository.setChecksumValidation(true);
        repository1.setRemoteRepository(remoteRepository);

        Storage storage = configurationManagementService.getStorage("storage0");
        repository1.setStorage(storage);
        storage.addOrUpdateRepository(repository1);

        // test get request BEFORE initialize of remote repository
        String url = getContextBaseUrl() + "/storages/storage0/releases/.index/nexus-maven-repository-index.properties";

        given().header("user-agent", "Maven/*")
               .contentType(ContentType.TEXT)
               .when()
               .get(url)
               .peek()
               .then()
               .toString();

        repositoryManagementService.createRepository("storage0", "carlspring");
    }

    @After
    public void tearDown()
            throws IOException
    {
        repositoryManagementService.removeRepository(storageId, repositoryId);
        super.shutdown();
    }

    public void addRebuildCronJobConfig(String name)
            throws Exception
    {
        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", DownloadRemoteIndexCronJob.class.getName());
        cronTaskConfiguration.addProperty("cronExpression", "0 0/2 * 1/1 * ? *");
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

    @Test
    public void testDownloadRemoteRepositoryIndex()
            throws Exception
    {

        prepareTest();

        System.out.println("Index === " + repositoryIndexManager.getRepositoryIndex(storageId.concat(":")
                                                                                             .concat(repositoryId)));

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
                                                  "+g:org.carlspring.strongbox +a:strongbox-commons +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request));

        deleteRebuildCronJobConfig(jobName);

    }
}
