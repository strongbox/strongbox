package org.carlspring.strongbox.services;

import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

    public static final String REPOSITORYID = "artifact-search-service-test-releases";

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
                                      REPOSITORYID,
                                      true,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "1.0.1", "1.1.1", "1.2.1");
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
        repositories.add(mockRepositoryMock(STORAGE0, REPOSITORYID));

        return repositories;
    }

    @Test
    @Ignore
    public void testContains() throws Exception
    {
        final int x = repositoryManagementService.reIndex(STORAGE0,
                                                          REPOSITORYID,
                                                          "org/carlspring/strongbox/strongbox-utils");

        assertTrue("Incorrect number of artifacts found!", x >= 3);

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORYID,
                                                  "+g:org.carlspring.strongbox +a:strongbox-utils +v:1.0.1 +p:jar");

        artifactSearchService.contains(request);
    }

}
