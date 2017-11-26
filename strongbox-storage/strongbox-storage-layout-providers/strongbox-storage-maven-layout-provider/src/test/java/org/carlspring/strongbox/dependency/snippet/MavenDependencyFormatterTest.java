package org.carlspring.strongbox.dependency.snippet;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author carlspring
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class MavenDependencyFormatterTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "mdft-releases";

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;

    @Inject
    private MavenIndexerSearchProvider mavenIndexerSearchProvider;

    @Inject
    private ArtifactEntryService artifactEntryService;


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
        // Because there is no smarter way to cleanup... :-|
        removeEntriesIfAnyExist();

        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES,
                                      true,
                                      "org.carlspring.strongbox:maven-snippet",
                                      "1.0", "1.1", "1.2");

        createArtifactEntry(new MavenArtifactCoordinates("org.carlspring.strongbox",
                                                         "maven-snippet",
                                                         "1.0",
                                                         null,
                                                         "jar"),
                            STORAGE0,
                            REPOSITORY_RELEASES);
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        getRepositoryIndexManager().closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES);

        removeRepositories(getRepositoriesToClean());
    }

    private void removeEntriesIfAnyExist()
    {
        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates("org.carlspring.strongbox",
                                                                            "maven-snippet",
                                                                            "1.0",
                                                                            null,
                                                                            "jar");

        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact(STORAGE0,
                                                                           REPOSITORY_RELEASES,
                                                                           coordinates.toPath())
                                                          .orElse(null);

        if (artifactEntry != null)
        {
            artifactEntryService.delete(artifactEntry);
        }
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES));

        return repositories;
    }

    @Test
    public void testMavenDependencyGeneration()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            Maven2LayoutProvider.ALIAS);
        assertNotNull("Failed to look up dependency synonym formatter!", formatter);

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("1.0");
        coordinates.setExtension("jar");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertEquals("Failed to generate dependency!",
                     "<dependency>\n" +
                     "    <groupId>" + coordinates.getGroupId() + "</groupId>\n" +
                     "    <artifactId>" + coordinates.getArtifactId() + "</artifactId>\n" +
                     "    <version>" + coordinates.getVersion() + "</version>\n" +
                     "    <type>" + coordinates.getExtension() + "</type>\n" +
                     "    <scope>compile</scope>\n" +
                     "</dependency>\n",
                     snippet);
    }

    @Test
    public void testSearchExactWithDependencySnippet()
            throws IOException, SearchException
    {
        MavenRepositoryFeatures features = getFeatures();
        final int x = features.reIndex(STORAGE0,
                                       REPOSITORY_RELEASES,
                                       "org/carlspring/strongbox/maven-snippet");

        assertTrue("Incorrect number of artifacts found!", x >= 3);

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORY_RELEASES,
                                                  new MavenArtifactCoordinates("org.carlspring.strongbox",
                                                                               "maven-snippet",
                                                                               "1.0",
                                                                               null,
                                                                               "jar"),
                                                  MavenIndexerSearchProvider.ALIAS);

        SearchResult searchResult = mavenIndexerSearchProvider.findExact(request);

        assertFalse(searchResult.getSnippets().isEmpty());

        for (String key : searchResult.getSnippets().keySet())
        {
            System.out.println("Dependency snippet for " + key + ":");
            System.out.println("------------------------------------------------------");
            System.out.println(searchResult.getSnippets().get(key));
        }
    }

    public ArtifactEntry createArtifactEntry(ArtifactCoordinates coordinates,
                                             String storageId,
                                             String repositoryId)
    {
        ArtifactEntry artifactEntry = new ArtifactEntry();
        artifactEntry.setArtifactCoordinates(coordinates);
        artifactEntry.setStorageId(storageId);
        artifactEntry.setRepositoryId(repositoryId);

        return artifactEntryService.save(artifactEntry);
    }

}
