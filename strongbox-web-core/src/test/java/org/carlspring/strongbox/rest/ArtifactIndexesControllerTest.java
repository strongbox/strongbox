package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author Kate Novik
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ArtifactIndexesControllerTest
        extends RestAssuredBaseTest
{

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_INDEXES = "org/carlspring/strongbox/indexes/strongbox-test-one";

    private final static String STORAGE_ID = "storage-indexing-tests";

    public static final String REPOSITORY_RELEASES_1 = "aict-releases-1";

    public static final String REPOSITORY_RELEASES_2 = "aict-releases-2";

    @Inject
    private ArtifactSearchService artifactSearchService;


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
        super.init();

        // prepare storage: create it from Java code instead of putting <storage/> in strongbox.xml
        createStorage(STORAGE_ID);

        // Used by:
        // - testRebuildArtifactsIndexes()
        // - testRebuildIndexesInRepository()
        // - testRebuildIndexesInStorage()
        // - testRebuildIndexesInStorage()
        createRepository(STORAGE_ID, REPOSITORY_RELEASES_1, true);

        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_1).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0");

        // Used by testRebuildIndexesInStorage()
        createRepository(STORAGE_ID, REPOSITORY_RELEASES_2, true);

        generateArtifact(getRepositoryBasedir(STORAGE_ID, REPOSITORY_RELEASES_2).getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-two:1.0");
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE_ID, REPOSITORY_RELEASES_1));
        repositories.add(createRepositoryMock(STORAGE_ID, REPOSITORY_RELEASES_2));

        return repositories;
    }

    @Test
    public void testRebuildArtifactsIndexes()
            throws Exception
    {
        client.rebuildIndexes(STORAGE_ID, REPOSITORY_RELEASES_1, ARTIFACT_BASE_PATH_STRONGBOX_INDEXES);

        SearchRequest request = new SearchRequest(STORAGE_ID,
                                                  REPOSITORY_RELEASES_1,
                                                  "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar",
                                                  MavenIndexerSearchProvider.ALIAS);

        assertTrue(artifactSearchService.contains(request));
    }

    @Test
    public void testRebuildIndexesInRepository()
            throws Exception
    {
        client.rebuildIndexes(STORAGE_ID, REPOSITORY_RELEASES_1, null);

        SearchRequest request1 = new SearchRequest(STORAGE_ID,
                                                   REPOSITORY_RELEASES_1,
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar",
                                                   MavenIndexerSearchProvider.ALIAS);

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest(STORAGE_ID,
                                                   REPOSITORY_RELEASES_1,
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-two +v:1.0 +p:jar",
                                                   MavenIndexerSearchProvider.ALIAS);

        assertTrue(artifactSearchService.contains(request2));
    }

    @Test
    public void testRebuildIndexesInStorage()
            throws Exception
    {
        client.rebuildIndexes(STORAGE_ID);

        SearchRequest request1 = new SearchRequest(STORAGE_ID,
                                                   REPOSITORY_RELEASES_1,
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-two +v:1.0 +p:jar",
                                                   MavenIndexerSearchProvider.ALIAS);

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest(STORAGE_ID,
                                                   REPOSITORY_RELEASES_2,
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar",
                                                   MavenIndexerSearchProvider.ALIAS);

        assertTrue(artifactSearchService.contains(request2));
    }

}
