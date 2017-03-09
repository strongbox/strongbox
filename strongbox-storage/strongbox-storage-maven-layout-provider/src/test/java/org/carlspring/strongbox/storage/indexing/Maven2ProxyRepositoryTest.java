package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.MockedIndexResourceFetcherConfig;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.RepositoryInitializationException;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationAndIndexing;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
@ContextConfiguration(classes = MockedIndexResourceFetcherConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class Maven2ProxyRepositoryTest
        extends TestCaseWithArtifactGenerationAndIndexing
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

    @PostConstruct
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

    @PreDestroy
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
        // Make sure the repositoryReleases that is being proxied has a packed index to serve:
        repositoryManagementService.pack(STORAGE0, REPOSITORY_RELEASES);

        Repository repositoryReleases = configurationManager.getRepository(STORAGE0, REPOSITORY_RELEASES);
        File indexPropertiesFile = new File(repositoryReleases.getBasedir(),
                                            ".index/local/nexus-maven-repository-index.properties");

        assertTrue("Failed to produce packed index for " +
                   repositoryReleases.getStorage().getId() + ":" + repositoryReleases.getId() + "!",
                   indexPropertiesFile.exists());

        // Download the remote index for the proxy repository
        repositoryManagementService.downloadRemoteIndex(STORAGE0, REPOSITORY_PROXY);

        Repository repositoryProxiedReleases = configurationManager.getRepository(STORAGE0, REPOSITORY_PROXY);
        File indexPropertiesUpdaterFile = new File(repositoryProxiedReleases.getBasedir(),
                                            ".index/remote/nexus-maven-repository-index-updater.properties");

        assertTrue("Failed to retrieve nexus-maven-repositoryReleases-index-updater.properties from the remote!",
                   indexPropertiesUpdaterFile.exists());
    }

}
