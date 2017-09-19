package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.storage.indexing.downloader.IndexDownloader;
import org.carlspring.strongbox.storage.indexing.downloader.ResourceFetcherFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.maven.index.updater.ResourceFetcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class Maven2ProxyRepositoryTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "m2pr-releases";

    private static final String REPOSITORY_PROXY = "m2pr-proxied-releases";

    @Inject
    private ConfigurationManager configurationManager;

    @Mock
    private ResourceFetcherFactory resourceFetcherFactory;

    @Inject
    private ResourceFetcher resourceFetcher;

    @InjectMocks
    @Inject
    private IndexDownloader downloader;


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
        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES,
                                      true,
                                      "org.carlspring.strongbox:strongbox-search-test",
                                      "1.0", "1.1", "1.2");

        createProxyRepository(STORAGE0,
                              REPOSITORY_PROXY,
                              "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES + "/");

        MockitoAnnotations.initMocks(this);
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_PROXY));

        return repositories;
    }

    @Test
    public void testRepositoryIndexFetching()
            throws ArtifactTransportException, IOException
    {
        Mockito.when(resourceFetcherFactory.createIndexResourceFetcher(Matchers.anyString(), Matchers.any(
                CloseableHttpClient.class))).thenReturn(resourceFetcher);

        MavenRepositoryFeatures features = (MavenRepositoryFeatures) getFeatures(STORAGE0, REPOSITORY_RELEASES);

        // Make sure the repository that is being proxied has a packed index to serve:
        features.pack(STORAGE0, REPOSITORY_RELEASES);

        Repository repositoryReleases = configurationManager.getRepository(STORAGE0, REPOSITORY_RELEASES);
        File indexPropertiesFile = new File(repositoryReleases.getBasedir(),
                                            ".index/local/nexus-maven-repository-index.properties");

        assertTrue("Failed to produce packed index for " +
                   repositoryReleases.getStorage().getId() + ":" + repositoryReleases.getId() + "!",
                   indexPropertiesFile.exists());

        // Download the remote index for the proxy repository
        features.downloadRemoteIndex(STORAGE0, REPOSITORY_PROXY);

        Repository repositoryProxiedReleases = configurationManager.getRepository(STORAGE0, REPOSITORY_PROXY);
        File indexPropertiesUpdaterFile = new File(repositoryProxiedReleases.getBasedir(),
                                                   ".index/remote/nexus-maven-repository-index-updater.properties");

        assertTrue("Failed to retrieve nexus-maven-repository-index-updater.properties from the remote!",
                   indexPropertiesUpdaterFile.exists());
    }

}
