package org.carlspring.strongbox.dependency.snippet;

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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

/**
 * @author carlspring
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@Execution(SAME_THREAD)
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

    @BeforeEach
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

        generateArtifact(REPOSITORY_BASEDIR, "org.carlspring.strongbox:maven-snippet:1.0:jar:sources");

        reIndex(STORAGE0, REPOSITORY_RELEASES, "org/carlspring/strongbox");
    }

    @AfterEach
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositories());
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
                                                         "1.0",
                                                         "sources",
                                                         "jar"));
    }

    private void removeEntryIfExists(MavenArtifactCoordinates coordinates1)
    {
        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact(STORAGE0,
                                                                           REPOSITORY_RELEASES,
                                                                           coordinates1.toPath());

        if (artifactEntry != null)
        {
            artifactEntryService.delete(artifactEntry);
        }
    }

    private Set<MutableRepository> getRepositories()
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
        assertNotNull(formatter, "Failed to look up dependency synonym formatter!");

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("1.0");
        coordinates.setExtension("jar");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertEquals("<dependency>\n" +
                     "    <groupId>org.carlspring.strongbox</groupId>\n" +
                     "    <artifactId>maven-snippet</artifactId>\n" +
                     "    <version>1.0</version>\n" +
                     "    <type>jar</type>\n" +
                     "    <scope>compile</scope>\n" +
                     "</dependency>\n",
                     snippet,
                     "Failed to generate dependency!");
    }

    @Test
    public void testMavenDependencyGenerationWithClassifier()
            throws ProviderImplementationException
    {
        DependencySynonymFormatter formatter = compatibleDependencyFormatRegistry.getProviderImplementation(Maven2LayoutProvider.ALIAS,
                                                                                                            Maven2LayoutProvider.ALIAS);
        assertNotNull(formatter, "Failed to look up dependency synonym formatter!");

        MavenArtifactCoordinates coordinates = new MavenArtifactCoordinates();
        coordinates.setGroupId("org.carlspring.strongbox");
        coordinates.setArtifactId("maven-snippet");
        coordinates.setVersion("2.0");
        coordinates.setClassifier("sources");

        String snippet = formatter.getDependencySnippet(coordinates);

        System.out.println(snippet);

        assertEquals("<dependency>\n" +
                     "    <groupId>org.carlspring.strongbox</groupId>\n" +
                     "    <artifactId>maven-snippet</artifactId>\n" +
                     "    <version>2.0</version>\n" +
                     "    <type>jar</type>\n" +
                     "    <classifier>sources</classifier>\n" +
                     "    <scope>compile</scope>\n" +
                     "</dependency>\n",
                     snippet,
                     "Failed to generate dependency!");
    }

    @Test
    public void testSearchExactWithDependencySnippet()
    {
        Assumptions.assumeTrue(mavenIndexerSearchProvider.isPresent());

        IndexedMavenRepositoryFeatures features = (IndexedMavenRepositoryFeatures) getFeatures();
        final int x = features.reIndex(STORAGE0,
                                       REPOSITORY_RELEASES,
                                       "org/carlspring/strongbox/maven-snippet");

        assertTrue(x >= 3, "Incorrect number of artifacts found!");

        SearchRequest request = new SearchRequest(STORAGE0,
                                                  REPOSITORY_RELEASES,
                                                  new MavenArtifactCoordinates("org.carlspring.strongbox",
                                                                               "maven-snippet",
                                                                               "1.0",
                                                                               null,
                                                                               "jar"),
                                                  MavenIndexerSearchProvider.ALIAS);

        SearchResult searchResult = mavenIndexerSearchProvider.get().findExact(request);

        assertNotNull(searchResult);
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

        assertNotNull(codeSnippets, "Failed to look up dependency synonym formatter!");
        assertFalse(codeSnippets.isEmpty(), "No synonyms found!");
        assertEquals(5, codeSnippets.size(), "Incorrect number of dependency synonyms!");

        String[] synonyms = new String[]{ "Maven 2", "Gradle", "Ivy", "Leiningen", "SBT" };

        int i = 0;
        for (CodeSnippet snippet : codeSnippets)
        {
            System.out.println(snippet.getName());

            assertEquals(synonyms[i], snippet.getName(), "Failed to re-order correctly!");

            i++;
        }
    }

}
