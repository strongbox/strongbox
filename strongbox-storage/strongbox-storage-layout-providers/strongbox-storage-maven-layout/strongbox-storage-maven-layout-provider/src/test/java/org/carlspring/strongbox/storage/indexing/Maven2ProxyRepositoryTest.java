package org.carlspring.strongbox.storage.indexing;

import static org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates.LAYOUT_NAME;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;

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

    private static final String PROXY_REPOSITORY_URL = "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES + "/";

    private static final String REPOSITORY_PROXY = "m2pr-proxied-releases";

    private static final String A1 = "org/carlspring/strongbox/strongbox-search-test/1.0/strongbox-search-test-1.0.jar";
    
    private static final String A2 = "org/carlspring/strongbox/strongbox-search-test/1.1/strongbox-search-test-1.1.jar";
    
    private static final String A3 = "org/carlspring/strongbox/strongbox-search-test/1.2/strongbox-search-test-1.2.jar";

    @Inject
    private RepositoryPathResolver repositoryPathResolver;
    
    @Test
    @EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
    @ExtendWith({RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class})
    public void testRepositoryIndexFetching(@TestRepository(layout = LAYOUT_NAME, repositoryId = REPOSITORY_RELEASES, setup = MavenIndexedRepositorySetup.class) Repository repository,
                                            @TestRepository(layout = LAYOUT_NAME, repositoryId = REPOSITORY_PROXY, setup = MavenIndexedRepositorySetup.class) @Remote(url = PROXY_REPOSITORY_URL ) Repository proxyRepository,
                                            @TestArtifact(repositoryId = REPOSITORY_RELEASES, resource = A1, generator = MavenArtifactGenerator.class) Path a1,
                                            @TestArtifact(repositoryId = REPOSITORY_RELEASES, resource = A2, generator = MavenArtifactGenerator.class) Path a2,
                                            @TestArtifact(repositoryId = REPOSITORY_RELEASES, resource = A3, generator = MavenArtifactGenerator.class) Path a3)
            throws ArtifactTransportException, IOException
    {
        IndexedMavenRepositoryFeatures features = (IndexedMavenRepositoryFeatures) getFeatures();

        // Make sure the repository that is being proxied has a packed index to serve:
        features.pack(STORAGE0, REPOSITORY_RELEASES);

        Path indexPropertiesFile = repositoryPathResolver.resolve(repository).resolve(".index/local/nexus-maven-repository-index.properties");
        assertTrue(Files.exists(indexPropertiesFile),
                   "Failed to produce packed index for " +
                           repository.getStorage().getId() + ":" + repository.getId() + "!");

        // Download the remote index for the proxy repository
        features.downloadRemoteIndex(STORAGE0, REPOSITORY_PROXY);

        
        Path indexPropertiesUpdaterFile = repositoryPathResolver.resolve(proxyRepository).resolve(".index/remote/nexus-maven-repository-index-updater.properties");
        assertTrue(Files.exists(indexPropertiesUpdaterFile),
                   "Failed to retrieve nexus-maven-repository-index-updater.properties from the remote!");
    }

}
