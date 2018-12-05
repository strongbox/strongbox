package org.carlspring.strongbox.services;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author mtodorov
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class MavenRepositoryManagementServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String STORAGES_BASEDIR = ConfigurationResourceResolver.getVaultDirectory() + "/storages";

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

    @BeforeEach
    public void initialize()
            throws Exception
    {
        createRepository(STORAGE0, REPOSITORY_RELEASES_1, true);

        createRepository(STORAGE0, REPOSITORY_RELEASES_2, true);

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
    }

    @AfterEach
    public void removeRepositories()
            throws IOException, JAXBException
    {
        closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES_1);
        closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES_2);
        closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES_MERGE_1);
        closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES_MERGE_2);
        removeRepositories(getRepositoriesToClean());
    }

    @Test
    public void testCreateRepository()
    {
        File repositoryBaseDir = new File(STORAGES_BASEDIR, STORAGE0 + "/" + REPOSITORY_RELEASES_1);

        assertTrue(repositoryBaseDir.exists(), "Failed to create repository '" + REPOSITORY_RELEASES_1 + "'!");
    }

    @Test
    public void testCreateAndDelete()
            throws Exception
    {
        File basedir = new File(STORAGES_BASEDIR + "/" + STORAGE0);
        File repositoryDir = new File(basedir, REPOSITORY_RELEASES_2);

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
