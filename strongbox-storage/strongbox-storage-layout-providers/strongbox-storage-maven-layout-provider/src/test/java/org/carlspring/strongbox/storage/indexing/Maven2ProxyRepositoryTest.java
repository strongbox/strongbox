package org.carlspring.strongbox.storage.indexing;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_PROXY, Maven2LayoutProvider.ALIAS));

        return repositories;
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
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        closeIndexersForRepository(STORAGE0, REPOSITORY_PROXY);
        closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES);
        removeRepositories(getRepositoriesToClean());
    }

    @Test
    public void testRepositoryIndexFetching()
            throws ArtifactTransportException, IOException
    {
        Assume.assumeTrue(repositoryIndexManager.isPresent());

        IndexedMavenRepositoryFeatures features = (IndexedMavenRepositoryFeatures) getFeatures();

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
