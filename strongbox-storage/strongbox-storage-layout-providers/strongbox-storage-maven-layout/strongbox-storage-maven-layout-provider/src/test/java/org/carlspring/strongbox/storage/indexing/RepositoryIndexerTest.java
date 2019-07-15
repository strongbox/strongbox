package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.index.ArtifactInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private static final String GROUP_ID = "org.carlspring.strongbox";
    private static final String ARTIFACT_ID = "strongbox-commons";
    private static final String ARTIFACT_BASE_PATH_STRONGBOX = "org/carlspring/strongbox/strongbox-commons";

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testIndex(@MavenRepository(repositoryId = REPOSITORY_RELEASES,
                                           setup = MavenIndexedRepositorySetup.class)
                          Repository repository,
                          @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                             id = GROUP_ID + ":" + ARTIFACT_ID,
                                             versions = { "1.0",
                                                          "1.1",
                                                          "1.2" })
                          List<Path> artifactPaths)
            throws Exception
    {
        RepositoryIndexManager repositoryIndexManager = this.repositoryIndexManager.get();
        RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndexer(STORAGE0 + ":" +
                                                                                          repository.getId() + ":" +
                                                                                          IndexTypeEnum.LOCAL.getType());

        IndexedMavenRepositoryFeatures features = (IndexedMavenRepositoryFeatures) getFeatures();

        int numberOfFiles = features.reIndex(STORAGE0, repository.getId(), ARTIFACT_BASE_PATH_STRONGBOX);

        features.pack(STORAGE0, repository.getId());

        final RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        final Path indexPacked = repositoryPath.resolve(".index/local/nexus-maven-repository-index.gz");
        assertTrue(Files.exists(indexPacked), "Failed to pack index!");

        final Path indexProperties = repositoryPath.resolve(".index/local/nexus-maven-repository-index-packer.properties");
        assertTrue(Files.exists(indexProperties), "Failed to pack index!");

        assertEquals(6,

                     numberOfFiles,  // one is jar another pom, both would be added into the same Lucene document
                     "6 artifacts expected!");

        Set<SearchResult> search = repositoryIndexer.search("org.carlspring.strongbox",
                                                            "strongbox-commons",
                                                            null,
                                                            null,
                                                            null);

        assertEquals(3, search.size(), "Only three versions of the strongbox-commons artifact were expected!");

        search = repositoryIndexer.search(GROUP_ID, ARTIFACT_ID, "1.0", "jar", null);
        assertEquals(1, search.size(), GROUP_ID + ":" + ARTIFACT_ID + ":1.0 should not been deleted!");

        final String query = String.format("+g:%s +a:%s +v:1.0", GROUP_ID, ARTIFACT_ID);
        search = repositoryIndexer.search(query);
        assertEquals(2, search.size(), GROUP_ID + ":" + ARTIFACT_ID + ":1.0  should not been deleted!");

        repositoryIndexer.delete(asArtifactInfo(search));
        search = repositoryIndexer.search(GROUP_ID, ARTIFACT_ID, "1.0", null, null);

        assertEquals(0, search.size(), GROUP_ID + ":" + ARTIFACT_ID + ":1.0 should have been deleted!");
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
