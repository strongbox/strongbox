package org.carlspring.strongbox.services;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class ArtifactSearchServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "assi-releases";

    @Inject
    private ArtifactSearchService artifactSearchService;


    @Test
    @EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testContains(@MavenRepository(repositoryId = REPOSITORY_RELEASES,
                                              setup = MavenIndexedRepositorySetup.class)
                             Repository repository,
                             @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                id = "org.carlspring.strongbox:strongbox-utils",
                                                versions = { "1.0.1",
                                                             "1.1.1",
                                                             "1.2.1" })
                            List<Path> paths)
            throws Exception
    {
        final String repositoryId = repository.getId();

        IndexedMavenRepositoryFeatures features = (IndexedMavenRepositoryFeatures) getFeatures();
        final int x = features.reIndex(STORAGE0, repositoryId, "org/carlspring/strongbox/strongbox-utils");

        assertTrue(x >= 3, "Incorrect number of artifacts found!");

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repositoryId,
                                                  "+g:org.carlspring.strongbox +a:strongbox-utils +v:1.0.1 +p:jar",
                                                  MavenIndexerSearchProvider.ALIAS);

        artifactSearchService.contains(request);
    }

}
