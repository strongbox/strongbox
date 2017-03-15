package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.services.ArtifactSearchService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Ignore;
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
    private RepositoryIndexManager repositoryIndexManager;

    public final static String STORAGE_IDX_TEST = "storage-indexing-tests";

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }


    @Override
    public void init()
            throws Exception
    {
        super.init();

        // Used by:
        // - testRebuildArtifactsIndexes()
        // - testRebuildIndexesInRepository()
        // - testRebuildIndexesInStorage()
        // - testRebuildIndexesInStorage()
        createRepository(STORAGE_IDX_TEST, "aict-releases-1", true);

        generateArtifact(getRepositoryBasedir(STORAGE_IDX_TEST, "aict-releases-1").getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0");

        generateArtifact(getRepositoryBasedir(STORAGE_IDX_TEST, "aict-releases-1").getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-two:1.0");

        // Used by testRebuildIndexesInStorage()
        createRepository(STORAGE_IDX_TEST, "aict-releases-2", true);

        generateArtifact(getRepositoryBasedir(STORAGE_IDX_TEST, "aict-releases-2").getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-one:1.0");

        generateArtifact(getRepositoryBasedir(STORAGE_IDX_TEST, "aict-releases-2").getAbsolutePath(),
                         "org.carlspring.strongbox.indexes:strongbox-test-two:1.0");

        // Used by testRebuildIndexesInStorages()
        createRepositoryWithArtifacts(STORAGE_IDX_TEST,
                                      "aict-releases-3",
                                      true,
                                      "org.carlspring.strongbox.indexes:strongbox-test-one", "1.0");
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
        repositories.add(createRepositoryMock(STORAGE_IDX_TEST, "aict-releases-1"));
        repositories.add(createRepositoryMock(STORAGE_IDX_TEST, "aict-releases-2"));
        repositories.add(createRepositoryMock(STORAGE_IDX_TEST, "aict-releases-3"));

        return repositories;
    }

    @Test
    public void testRebuildArtifactsIndexes()
            throws Exception
    {
        client.rebuildIndexes(STORAGE_IDX_TEST, "aict-releases-1", ARTIFACT_BASE_PATH_STRONGBOX_INDEXES);

        SearchRequest request = new SearchRequest(STORAGE_IDX_TEST,
                                                  "aict-releases-1",
                                                  "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request));
    }

    @Test
    public void testRebuildIndexesInRepository()
            throws Exception
    {
        client.rebuildIndexes(STORAGE_IDX_TEST, "aict-releases-1", null);

        SearchRequest request1 = new SearchRequest(STORAGE_IDX_TEST,
                                                   "aict-releases-1",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest(STORAGE_IDX_TEST,
                                                   "aict-releases-1",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-two +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));

    }

    @Test
    public void testRebuildIndexesInStorage()
            throws Exception
    {
        client.rebuildIndexes(STORAGE_IDX_TEST);

        SearchRequest request1 = new SearchRequest(STORAGE_IDX_TEST,
                                                   "aict-releases-1",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-two +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest(STORAGE_IDX_TEST,
                                                   "aict-releases-2",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));
    }

    @Test
    @Ignore
    /*  I think ArtifactIndexesControllerTest.testRebuildIndexesInStorages() should be removed at all. Because
        ArtifactIndexesControllerTest need to take care only on some specific isolated storages but not on all of them
        otherwise this test can’t be executed in parallel
     */
    public void testRebuildIndexesInStorages()
            throws Exception
    {
        client.rebuildIndexes();

        SearchRequest request1 = new SearchRequest(STORAGE_IDX_TEST,
                                                   "aict-releases-1",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request1));

        SearchRequest request2 = new SearchRequest(STORAGE_IDX_TEST,
                                                   "aict-releases-3",
                                                   "+g:org.carlspring.strongbox.indexes +a:strongbox-test-one +v:1.0 +p:jar");

        assertTrue(artifactSearchService.contains(request2));
    }

}
