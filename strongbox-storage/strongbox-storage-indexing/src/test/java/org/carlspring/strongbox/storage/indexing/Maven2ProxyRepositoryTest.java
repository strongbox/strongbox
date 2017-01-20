package org.carlspring.strongbox.storage.indexing;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.MockedIndexResourceFetcherConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.RepositoryInitializationException;
import org.carlspring.strongbox.storage.repository.RemoteRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author carlspring
 */
@ContextConfiguration(classes = MockedIndexResourceFetcherConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class Maven2ProxyRepositoryTest
        extends TestCaseWithArtifactGenerationWithIndexing
{

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases");

    private static final File INDEX_DIR = new File(REPOSITORY_BASEDIR, ".index");

    @Autowired
    private ConfigurationManagementService configurationManagementService;

    @Autowired
    private RepositoryManagementService repositoryManagementService;


    @Before
    public void init()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException,
                   ArtifactOperationException,
                   JAXBException, InterruptedException
    {
        //noinspection ResultOfMethodCallIgnored
        INDEX_DIR.mkdirs();

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-search-test:1.0:jar");
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-search-test:1.1:jar");
        Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-search-test:1.2:jar");

        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact1);
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact2);
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact3);

        createProxyRepository();
    }

    private void createProxyRepository()
            throws IOException, JAXBException, InterruptedException
    {
        String storageId = "storage0";
        String repositoryId = "test-search-releases-001";

        RemoteRepository remoteRepository = new RemoteRepository();
        remoteRepository.setUrl("http://localhost:48080/storages/storage0/releases/");

        Repository repository = new Repository(repositoryId);
        repository.setRemoteRepository(remoteRepository);
        repository.setIndexingEnabled(true);
        repository.setStorage(configurationManagementService.getStorage(storageId));

        configurationManagementService.addOrUpdateRepository(storageId, repository);

        final File repositoryBaseDir = new File(repository.getBasedir());
        if (!repositoryBaseDir.exists())
        {
            repositoryManagementService.createRepository(storageId, repository.getId());
        }

        // Create the repository
        repositoryManagementService.createRepository(storageId, repositoryId);
        // Re-index it, so that the generated artifacts could be added to the index
        repositoryManagementService.reIndex("storage0", "releases", "org/carlspring");
        // Pack the index, so that it could be downloaded by the proxy repository
        repositoryManagementService.pack(storageId, "releases");
    }

    @Test
    public void testRepositoryIndexFetching()
            throws ArtifactTransportException, RepositoryInitializationException
    {
        System.out.println("Foo!!!");
        repositoryManagementService.downloadRemoteIndex("storage0", "test-search-releases-001");
    }

}
