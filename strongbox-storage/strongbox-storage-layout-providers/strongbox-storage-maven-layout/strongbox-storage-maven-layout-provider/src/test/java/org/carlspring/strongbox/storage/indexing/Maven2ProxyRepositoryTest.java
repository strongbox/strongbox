package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author carlspring
 */
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class Maven2ProxyRepositoryTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "m2pr-releases";

    private static final String REPOSITORY_PROXY = "m2pr-proxied-releases";

    @Inject
    private ConfigurationManager configurationManager;

    @BeforeAll
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

    @BeforeEach
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

    @AfterEach
    public void removeRepositories()
            throws Exception
    {
        removeRepositories(getRepositoriesToClean());
    }

    @Test
    @EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
    @Execution(CONCURRENT)
    public void testRepositoryIndexFetching()
            throws ArtifactTransportException, IOException
    {
        IndexedMavenRepositoryFeatures features = (IndexedMavenRepositoryFeatures) getFeatures();

        // Make sure the repository that is being proxied has a packed index to serve:
        features.pack(STORAGE0, REPOSITORY_RELEASES);

        Repository repositoryReleases = configurationManager.getRepository(STORAGE0, REPOSITORY_RELEASES);
        File indexPropertiesFile = new File(repositoryReleases.getBasedir(),
                                            ".index/local/nexus-maven-repository-index.properties");

        assertTrue(indexPropertiesFile.exists(),
                   "Failed to produce packed index for " +
                           repositoryReleases.getStorage().getId() + ":" + repositoryReleases.getId() + "!");

        // Download the remote index for the proxy repository
        features.downloadRemoteIndex(STORAGE0, REPOSITORY_PROXY);

        Repository repositoryProxiedReleases = configurationManager.getRepository(STORAGE0, REPOSITORY_PROXY);
        File indexPropertiesUpdaterFile = new File(repositoryProxiedReleases.getBasedir(),
                                                   ".index/remote/nexus-maven-repository-index-updater.properties");

        assertTrue(indexPropertiesUpdaterFile.exists(),
                   "Failed to retrieve nexus-maven-repository-index-updater.properties from the remote!");
    }

}
