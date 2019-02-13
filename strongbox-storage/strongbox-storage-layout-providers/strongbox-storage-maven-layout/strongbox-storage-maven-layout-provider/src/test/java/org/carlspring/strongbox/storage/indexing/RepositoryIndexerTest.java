package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.index.ArtifactInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
@EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
@Execution(CONCURRENT)
public class RepositoryIndexerTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "ri-releases";

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
                                      REPOSITORY_RELEASES,
                                      true,
                                      "org.carlspring.strongbox:strongbox-commons",
                                      "1.0", "1.1", "1.2");
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
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testIndex() throws Exception
    {
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();
        RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndexer(STORAGE0 + ":" +
                                                                                          REPOSITORY_RELEASES + ":" +
                                                                                          IndexTypeEnum.LOCAL.getType());

        IndexedMavenRepositoryFeatures features = (IndexedMavenRepositoryFeatures) getFeatures();

        int x = features.reIndex(STORAGE0, REPOSITORY_RELEASES, "org/carlspring/strongbox/strongbox-commons");

        features.pack(STORAGE0, REPOSITORY_RELEASES);

        File repositoryBasedir = getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES);

        assertTrue(new File(repositoryBasedir.getAbsolutePath(),
                            ".index/local/nexus-maven-repository-index.gz").exists(), "Failed to pack index!");
        assertTrue(new File(repositoryBasedir.getAbsolutePath(),
                            ".index/local/nexus-maven-repository-index-packer.properties").exists(),
                   "Failed to pack index!");

        assertEquals(6,

                     x,  // one is jar another pom, both would be added into the same Lucene document
                     "6 artifacts expected!");

        Set<SearchResult> search = repositoryIndexer.search("org.carlspring.strongbox",
                                                            "strongbox-commons",
                                                            null,
                                                            null,
                                                            null);

        assertEquals(3, search.size(), "Only three versions of the strongbox-commons artifact were expected!");

        search = repositoryIndexer.search("org.carlspring.strongbox", "strongbox-commons", "1.0", "jar", null);
        assertEquals(1, search.size(), "org.carlspring.strongbox:strongbox-commons:1.0 should not been deleted!");

        search = repositoryIndexer.search("+g:org.carlspring.strongbox +a:strongbox-commons +v:1.0");
        assertEquals(2, search.size(), "org.carlspring.strongbox:strongbox-commons:1.0 should not been deleted!");

        repositoryIndexer.delete(asArtifactInfo(search));
        search = repositoryIndexer.search("org.carlspring.strongbox", "strongbox-commons", "1.0", null, null);

        assertEquals(0, search.size(), "org.carlspring.strongbox:strongbox-commons:1.0 should have been deleted!");
    }

    private Collection<ArtifactInfo> asArtifactInfo(Set<SearchResult> results)
    {
        Collection<ArtifactInfo> artifactInfos = new LinkedHashSet<>();
        for (SearchResult result : results)
        {
            MavenArtifactCoordinates mavenArtifactCoordinates = (MavenArtifactCoordinates) result.getArtifactCoordinates();
            artifactInfos.add(new ArtifactInfo(result.getRepositoryId(),
                                               mavenArtifactCoordinates.getGroupId(),
                                               mavenArtifactCoordinates.getArtifactId(),
                                               mavenArtifactCoordinates.getVersion(),
                                               mavenArtifactCoordinates.getClassifier(),
                                               mavenArtifactCoordinates.getExtension()));
        }

        return artifactInfos;
    }

}
