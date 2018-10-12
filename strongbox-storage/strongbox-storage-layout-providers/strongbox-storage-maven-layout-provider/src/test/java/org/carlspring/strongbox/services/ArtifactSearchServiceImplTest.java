package org.carlspring.strongbox.services;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assume;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author mtodorov
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class ArtifactSearchServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    public static final String REPOSITORYID = "artifact-search-service-test-releases";

    @Inject
    private ArtifactSearchService artifactSearchService;

    @Inject
    private RepositoryManagementService repositoryManagementService;


    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @BeforeEach
    public void initialize()
            throws Exception
    {
        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORYID,
                                      true,
                                      "org.carlspring.strongbox:strongbox-utils",
                                      "1.0.1", "1.1.1", "1.2.1");
    }

    @AfterEach
    public void removeRepositories()
            throws IOException, JAXBException
    {
        closeIndexersForRepository(STORAGE0, REPOSITORYID);
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORYID, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testContains() throws Exception
    {
        Assume.assumeTrue(repositoryIndexManager.isPresent());

        IndexedMavenRepositoryFeatures features = (IndexedMavenRepositoryFeatures) getFeatures();
        final int x = features.reIndex(STORAGE0, REPOSITORYID, "org/carlspring/strongbox/strongbox-utils");

        assertTrue(x >= 3, "Incorrect number of artifacts found!");

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORYID,
                                                  "+g:org.carlspring.strongbox +a:strongbox-utils +v:1.0.1 +p:jar",
                                                  MavenIndexerSearchProvider.ALIAS);

        artifactSearchService.contains(request);
    }

}
