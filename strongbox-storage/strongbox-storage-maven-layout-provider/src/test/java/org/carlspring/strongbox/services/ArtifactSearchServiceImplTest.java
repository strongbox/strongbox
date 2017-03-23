package org.carlspring.strongbox.services;

import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class ArtifactSearchServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    public static final String REPOSITORYID = "artifact-search-service-test-releases";

    @Inject
    private ArtifactSearchService artifactSearchService;

    @Inject
    private RepositoryManagementService repositoryManagementService;


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
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORYID));

        return repositories;
    }

    @Test
    public void testContains() throws Exception
    {
        MavenRepositoryFeatures features = (MavenRepositoryFeatures) getFeatures(STORAGE0, REPOSITORYID);
        final int x = features.reIndex(STORAGE0, REPOSITORYID, "org/carlspring/strongbox/strongbox-utils");

        assertTrue("Incorrect number of artifacts found!", x >= 3);

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORYID,
                                                  "+g:org.carlspring.strongbox +a:strongbox-utils +v:1.0.1 +p:jar");

        artifactSearchService.contains(request);
    }

}
