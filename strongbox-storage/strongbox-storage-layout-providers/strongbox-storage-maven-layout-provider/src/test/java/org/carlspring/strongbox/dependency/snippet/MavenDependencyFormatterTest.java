package org.carlspring.strongbox.dependency.snippet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.providers.search.MavenIndexerSearchProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.search.SearchRequest;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.*;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author carlspring
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class MavenDependencyFormatterTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "mdft-releases";

    public static final String REPOSITORY_BASEDIR = "target/strongbox-vault/storages/storage0/" + REPOSITORY_RELEASES;

    @Inject
    private CompatibleDependencyFormatRegistry compatibleDependencyFormatRegistry;

    @Inject
    private Optional<MavenIndexerSearchProvider> mavenIndexerSearchProvider;

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private SnippetGenerator snippetGenerator;


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
                                      "1.0");

        generateArtifact(REPOSITORY_BASEDIR, "org.carlspring.strongbox:maven-snippet:jar:sources");

        reIndex(STORAGE0, REPOSITORY_RELEASES, "org/carlspring/strongbox");
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES);

        removeRepositories(getRepositoriesToClean());
    }

    private void removeEntriesIfAnyExist()
    {
        removeEntryIfExists(new MavenArtifactCoordinates("org.carlspring.strongbox",
                                                         "maven-snippet",
                                                         "1.0",
                                                         null,
                                                         "jar"));
        removeEntryIfExists(new MavenArtifactCoordinates("org.carlspring.strongbox",
                                                         "maven-snippet",
                                                         "2.0",
                                                         "sources",
                                                         "jar"));
    }

    private void removeEntryIfExists(MavenArtifactCoordinates coordinates1)
    {
        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact(STORAGE0,
                                                                           REPOSITORY_RELEASES,
                                                                           coordinates1.toPath())
                                                          .orElse(null);

        if (artifactEntry != null)
        {
            artifactEntryService.delete(artifactEntry);
        }
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));

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
                     "    <groupId>org.carlspring.strongbox</groupId>\n" +
                     "    <artifactId>maven-snippet</artifactId>\n" +
                     "    <version>1.0</version>\n" +
                     "    <type>jar</type>\n" +
                     "    <scope>compile</scope>\n" +
                     "</dependency>\n",
                     snippet);
    }

    @Test
    public void testMavenDependencyGenerationWithClassifier()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            Maven2LayoutProvider.ALIAS);
        assertNotNull("Failed to look up dependency synonym formatter!", formatter);

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("2.0");
        coordinates.setClassifier("sources");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertEquals("Failed to generate dependency!",
                     "<dependency>\n" +
                     "    <groupId>org.carlspring.strongbox</groupId>\n" +
                     "    <artifactId>maven-snippet</artifactId>\n" +
                     "    <version>2.0</version>\n" +
                     "    <type>jar</type>\n" +
                     "    <classifier>sources</classifier>\n" +
                     "    <scope>compile</scope>\n" +
                     "</dependency>\n",
                     snippet);
    }

    @Test
    public void testSearchExactWithDependencySnippet()
    {
        Assume.assumeTrue(mavenIndexerSearchProvider.isPresent());

        IndexedMavenRepositoryFeatures features = (IndexedMavenRepositoryFeatures) getFeatures();
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

        SearchResult searchResult = mavenIndexerSearchProvider.get().findExact(request);

        assertFalse(searchResult.getSnippets().isEmpty());

        for (CodeSnippet codeSnippet : searchResult.getSnippets())
        {
            System.out.println("Dependency snippet for " + codeSnippet.name + ":");
            System.out.println("------------------------------------------------------");
            System.out.println(codeSnippet.code);
        }
    }

    @Test
    public void testRegisteredSynonyms()
    {
        List<CodeSnippet> codeSnippets = snippetGenerator.generateSnippets(Maven2LayoutProvider.ALIAS,
                                                                           new MavenArtifactCoordinates("org.carlspring.strongbox",
                                                                                                        "maven-snippet",
                                                                                                        "1.0",
                                                                                                        null,
                                                                                                        "jar"));

        assertNotNull("Failed to look up dependency synonym formatter!", codeSnippets);
        assertFalse("No synonyms found!", codeSnippets.isEmpty());
        assertEquals("Incorrect number of dependency synonyms!", 5, codeSnippets.size());

        String[] synonyms = new String[]{ "Maven 2", "Gradle", "Ivy", "Leiningen", "SBT" };

        int i = 0;
        for (CodeSnippet snippet : codeSnippets)
        {
            System.out.println(snippet.getName());

            assertEquals("Failed to re-order correctly!", synonyms[i], snippet.getName());

            i++;
        }
    }

}
