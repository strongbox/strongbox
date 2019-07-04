package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.MavenArtifactGenerator;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;

import java.io.File;
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
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenRepositoryManagementServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES_1 = "rmsi-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "rmsi-releases-2";

    private static final String REPOSITORY_RELEASES_MERGE_1 = "rmsi-releases-merge-1";

    private static final String REPOSITORY_RELEASES_MERGE_2 = "rmsi-releases-merge-2";

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testCreateRepository(@TestRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                     layout = MavenArtifactCoordinates.LAYOUT_NAME,
                                                     setup = MavenIndexedRepositorySetup.class)
                                     Repository repository)
            throws Exception
    {
        File repositoryBaseDir = getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES_1).getAbsoluteFile();

        System.out.println(repositoryBaseDir.getAbsolutePath());

        assertTrue(repositoryBaseDir.exists(), "Failed to create repository '" + REPOSITORY_RELEASES_1 + "'!");
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testCreateAndDelete(@TestRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                    layout = MavenArtifactCoordinates.LAYOUT_NAME,
                                                    policy = RepositoryPolicyEnum.RELEASE,
                                                    setup = MavenIndexedRepositorySetup.class,
                                                    cleanup = false)
                                    Repository repository)
            throws Exception
    {
        File repositoryDir = getRepositoryBasedir(STORAGE0,  REPOSITORY_RELEASES_1).getAbsoluteFile();

        assertTrue(repositoryDir.exists(),
                   "Failed to create the repository \"" + repositoryDir.getAbsolutePath() + "\"!");

        closeIndexer(STORAGE0 + ":" + REPOSITORY_RELEASES_1 + ":" + IndexTypeEnum.LOCAL.getType());

        getRepositoryManagementService().removeRepository(STORAGE0, REPOSITORY_RELEASES_1);

        assertFalse(repositoryDir.exists(), "Failed to remove the repository!");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class, ArtifactManagementTestExecutionListener.class })
    @Test
    @EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
    public void testMerge(@TestRepository(repositoryId = REPOSITORY_RELEASES_MERGE_1,
                                          layout = MavenArtifactCoordinates.LAYOUT_NAME,
                                          setup = MavenIndexedRepositorySetup.class)
                          Repository r1,
                          @TestRepository(repositoryId = REPOSITORY_RELEASES_MERGE_2,
                                          layout = MavenArtifactCoordinates.LAYOUT_NAME,
                                          setup = MavenIndexedRepositorySetup.class)
                          Repository r2,
                          @TestArtifact(repositoryId = REPOSITORY_RELEASES_MERGE_1,
                                        id = "org.carlspring.strongbox:strongbox-utils",
                                        versions = { "6.2.2" },
                                        generator = MavenArtifactGenerator.class)
                          List<Path> repositoryArtifact1,
                          @TestArtifact(repositoryId = REPOSITORY_RELEASES_MERGE_2,
                                        id = "org.carlspring.strongbox:strongbox-utils",
                                        versions = { "6.2.3" },
                                        generator = MavenArtifactGenerator.class)
                          List<Path> repositoryArtifact2)
            throws Exception
    {
        // dumpIndex(STORAGE0, REPOSITORY_RELEASES_MERGE_1, IndexTypeEnum.LOCAL.getType());

        // 1) Check that an exists in the first repository
        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORY_RELEASES_MERGE_1,
                                                  "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.2 +p:jar",
                                                  MavenIndexerSearchProvider.ALIAS);

        artifactSearchService.search(request);

        assertTrue(artifactSearchService.contains(request));

        // 2) Check that the artifact which exists in the second repository does not exist
        //    in the index of the first repository.
        request = new SearchRequest(STORAGE0,
                                    REPOSITORY_RELEASES_MERGE_1,
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.3 +p:jar",
                                    MavenIndexerSearchProvider.ALIAS);

        assertFalse(artifactSearchService.contains(request));

        IndexedMavenRepositoryFeatures features = (IndexedMavenRepositoryFeatures) getFeatures();

        // 3) Merge the second repository into the first one
        features.mergeIndexes(STORAGE0,
                              REPOSITORY_RELEASES_MERGE_2,
                              STORAGE0,
                              REPOSITORY_RELEASES_MERGE_1);

        // 4) Check that the merged repository now has both artifacts
        request = new SearchRequest(STORAGE0,
                                    REPOSITORY_RELEASES_MERGE_1,
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.3 +p:jar",
                                    MavenIndexerSearchProvider.ALIAS);

        assertTrue(artifactSearchService.contains(request), "Failed to merge!");
    }

}
