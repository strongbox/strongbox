package org.carlspring.strongbox.services;

import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author mtodorov
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(CONCURRENT)
public class ArtifactSearchServiceImplTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    public static final String REPOSITORYID = "artifact-search-service-test-releases";

    @Inject
    private ArtifactSearchService artifactSearchService;


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
            throws Exception
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORYID, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    @EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
    public void testContains() throws Exception
    {
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
