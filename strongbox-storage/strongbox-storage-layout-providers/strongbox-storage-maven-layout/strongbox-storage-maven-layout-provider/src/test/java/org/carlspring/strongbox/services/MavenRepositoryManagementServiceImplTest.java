package org.carlspring.strongbox.services;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
public class MavenRepositoryManagementServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES_1 = "rmsi-releases-1";

    private static final String REPOSITORY_RELEASES_MERGE_1 = "rmsi-releases-merge-1";

    private static final String REPOSITORY_RELEASES_MERGE_2 = "rmsi-releases-merge-2";

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testCreateRepository(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                      setup = MavenIndexedRepositorySetup.class)
                                     Repository repository)
    {
        Path repositoryPath = repositoryPathResolver.resolve(repository);
        assertTrue(Files.exists(repositoryPath), "Failed to create repository '" + repository.getId() + "'!");
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    @EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
    public void testCreateAndDelete(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                     setup = MavenIndexedRepositorySetup.class,
                                                     cleanup = false)
                                    Repository repository)
            throws Exception
    {
        Path repositoryPath = repositoryPathResolver.resolve(repository);
        assertTrue(Files.exists(repositoryPath), "Failed to create repository '" + repository.getId() + "'!");

        closeIndexer(STORAGE0 + ":" + repository.getId() + ":" + IndexTypeEnum.LOCAL.getType());

        getRepositoryManagementService().removeRepository(STORAGE0, REPOSITORY_RELEASES_1);

        assertTrue(Files.notExists(repositoryPath), "Failed to remove the repository!");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    @EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
    public void testMerge(@MavenRepository(repositoryId = REPOSITORY_RELEASES_MERGE_1,
                                           setup = MavenIndexedRepositorySetup.class)
                          Repository r1,
                          @MavenRepository(repositoryId = REPOSITORY_RELEASES_MERGE_2,
                                           setup = MavenIndexedRepositorySetup.class)
                          Repository r2,
                          @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_MERGE_1,
                                             id = "org.carlspring.strongbox:strongbox-utils",
                                             versions = "6.2.2")
                          List<Path> repositoryArtifact1,
                          @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_MERGE_2,
                                             id = "org.carlspring.strongbox:strongbox-utils",
                                             versions = "6.2.3")
                          List<Path> repositoryArtifact2)
            throws Exception
    {
        final String repository1Id = r1.getId();
        final String repository2Id = r2.getId();

        // 1) Check that an exists in the first repository
        SearchRequest request = new SearchRequest(STORAGE0,
                                                  repository1Id,
                                                  "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.2 +p:jar",
                                                  MavenIndexerSearchProvider.ALIAS);

        artifactSearchService.search(request);

        assertTrue(artifactSearchService.contains(request));

        // 2) Check that the artifact which exists in the second repository does not exist
        //    in the index of the first repository.
        request = new SearchRequest(STORAGE0,
                                    repository1Id,
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.3 +p:jar",
                                    MavenIndexerSearchProvider.ALIAS);

        assertFalse(artifactSearchService.contains(request));

        IndexedMavenRepositoryFeatures features = (IndexedMavenRepositoryFeatures) getFeatures();

        // 3) Merge the second repository into the first one
        features.mergeIndexes(STORAGE0,
                              repository2Id,
                              STORAGE0,
                              repository1Id);

        // 4) Check that the merged repository now has both artifacts
        request = new SearchRequest(STORAGE0,
                                    repository1Id,
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.3 +p:jar",
                                    MavenIndexerSearchProvider.ALIAS);

        assertTrue(artifactSearchService.contains(request), "Failed to merge!");
    }

}
