package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.booters.StorageBooter;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.SearchRequest;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author Kate Novik.
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ArtifactIndexesControllerTest
        extends RestAssuredBaseTest
{

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_INDEXES = "org/carlspring/strongbox/indexes/strongbox-test-one";

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private ArtifactSearchService artifactSearchService;

    @Inject
    private StorageBooter storageBooter;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;


    @Before
    public void setUp()
            throws Exception
    {
        super.init();

        // Used by:
        // - testRebuildArtifactsIndexes()
        // - testRebuildIndexesInRepository()
        // - testRebuildIndexesInStorage()
        // - testRebuildIndexesInStorage()
        createTestRepository(STORAGE0, "aict-releases-1", true);

        generateArtifact(getRepositoryBasedir(STORAGE0, "aict-releases-1").getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0");

        generateArtifact(getRepositoryBasedir(STORAGE0, "aict-releases-1").getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-two:1.0");

        // Used by testRebuildIndexesInStorage()
        createTestRepository(STORAGE0, "aict-releases-2", true);

        generateArtifact(getRepositoryBasedir(STORAGE0, "aict-releases-2").getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0");

        generateArtifact(getRepositoryBasedir(STORAGE0, "aict-releases-2").getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-two:1.0");

        // Used by testRebuildIndexesInStorages()
        createTestRepositoryWithArtifacts(STORAGE0,
                                          "aict-releases-3",
                                          true,
                                          "org.carlspring.strongbox.indexes:strongbox-test-one", "1.0");
    }

    @Override
    public Map<String, String> getRepositoriesToClean()
    {
        Map<String, String> repositories = new LinkedHashMap<>();
        repositories.put(STORAGE0, "aict-releases-1");
        repositories.put(STORAGE0, "aict-releases-2");
        repositories.put(STORAGE0, "aict-releases-3");

        return repositories;
    }

    @Test
    public void testRebuildArtifactsIndexes()
            throws Exception
    {
        client.rebuildIndexes(STORAGE0, "aict-releases-1", ARTIFACT_BASE_PATH_STRONGBOX_INDEXES);

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  "aict-releases-1",
                                                  "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request));
    }

    @Test
    public void testRebuildIndexesInRepository()
            throws Exception
    {
        client.rebuildIndexes(STORAGE0, "aict-releases-1", null);

        SearchRequest request1 = new SearchRequest(STORAGE0,
                                                   "aict-releases-1",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest(STORAGE0,
                                                   "aict-releases-1",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-two +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));

    }

    @Test
    public void testRebuildIndexesInStorage()
            throws Exception
    {
        client.rebuildIndexes(STORAGE0);

        SearchRequest request1 = new SearchRequest(STORAGE0,
                                                   "aict-releases-1",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-two +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest(STORAGE0,
                                                   "aict-releases-2",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));
    }

    @Test
    public void testRebuildIndexesInStorages()
            throws Exception
    {
        client.rebuildIndexes();

        SearchRequest request1 = new SearchRequest(STORAGE0,
                                                   "aict-releases-1",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest(STORAGE0,
                                                   "aict-releases-3",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));
    }

}
