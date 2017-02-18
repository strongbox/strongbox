package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ArtifactSearchServiceImplTest
        extends TestCaseWithArtifactGenerationWithIndexing
{

    @Autowired
    private ArtifactSearchService artifactSearchService;

    @Autowired
    private RepositoryManagementService repositoryManagementService;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Before
    public void setUp()
            throws Exception
    {
        createRepositoryWithArtifacts(STORAGE0,
                                      "artifact-search-service-test-releases",
                                      true,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "1.0.1", "1.1.1", "1.2.1");
    }

    public static Map<String, String> getRepositoriesToClean()
    {
        return null;
    }

    @Test
    public void testContains() throws Exception
    {
        final int x = repositoryManagementService.reIndex(STORAGE0,
                                                          "artifact-search-service-test-releases",
                                                          "org/carlspring/strongbox/strongbox-utils");

        assertTrue("Incorrect number of artifacts found!", x >= 3);

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  "artifact-search-service-test-releases",
                                                  "+g:org.carlspring.strongbox +a:strongbox-utils +v:1.0.1 +p:jar");

        artifactSearchService.contains(request);
    }

}
