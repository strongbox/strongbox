package org.carlspring.strongbox.services;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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


    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_1, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_2, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_MERGE_1, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES_MERGE_2, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testCreateRepository()
            throws Exception
    {
        createRepository(STORAGE0, REPOSITORY_RELEASES_1, true);

        File repositoryBaseDir = getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES_1).getAbsoluteFile();

        assertTrue(repositoryBaseDir.exists(), "Failed to create repository '" + REPOSITORY_RELEASES_1 + "'!");
    }

    @Test
    public void testCreateAndDelete()
            throws Exception
    {
        createRepository(STORAGE0, REPOSITORY_RELEASES_2, true);

        File repositoryDir = getRepositoryBasedir(STORAGE0,  REPOSITORY_RELEASES_2).getAbsoluteFile();

        assertTrue(repositoryDir.exists(),
                   "Failed to create the repository \"" + repositoryDir.getAbsolutePath() + "\"!");

        closeIndexer(STORAGE0 + ":" + REPOSITORY_RELEASES_2 + ":" + IndexTypeEnum.LOCAL.getType());

        getRepositoryManagementService().removeRepository(STORAGE0, REPOSITORY_RELEASES_2);

        assertFalse(repositoryDir.exists(), "Failed to remove the repository!");
    }

    @Test
    @EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
    public void testMerge()
            throws Exception
    {
        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES_MERGE_1,
                                      true,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "6.2.2");

        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES_MERGE_2,
                                      true,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "6.2.3");

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
