package org.carlspring.strongbox.storage.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.maven.index.ArtifactInfo;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.repository.IndexedMavenRepositoryFeatures;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.search.SearchResult;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class RepositoryIndexerTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_RELEASES = "ri-releases";

    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Before
    public void isIndexingEnabled()
    {
        Assume.assumeTrue(repositoryIndexManager.isPresent());
    }

    @Before
    public void initialize()
            throws Exception
    {
        createRepositoryWithArtifacts(STORAGE0,
                                      REPOSITORY_RELEASES,
                                      true,
                                      "org.carlspring.strongbox:strongbox-commons",
                                      "1.0", "1.1", "1.2");
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        closeIndexersForRepository(STORAGE0, REPOSITORY_RELEASES);
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

        assertTrue("Failed to pack index!", new File(repositoryBasedir.getAbsolutePath(),
                                                     ".index/local/nexus-maven-repository-index.gz").exists());
        assertTrue("Failed to pack index!", new File(repositoryBasedir.getAbsolutePath(),
                                                     ".index/local/nexus-maven-repository-index-packer.properties").exists());

        assertEquals("6 artifacts expected!",

                     6,  // one is jar another pom, both would be added into the same Lucene document
                     x);

        Set<SearchResult> search = repositoryIndexer.search("org.carlspring.strongbox",
                                                            "strongbox-commons",
                                                            null,
                                                            null,
                                                            null);

        assertEquals("Only three versions of the strongbox-commons artifact were expected!", 3, search.size());

        search = repositoryIndexer.search("org.carlspring.strongbox", "strongbox-commons", "1.0", "jar", null);
        assertEquals("org.carlspring.strongbox:strongbox-commons:1.0 should not been deleted!", 1, search.size());

        search = repositoryIndexer.search("+g:org.carlspring.strongbox +a:strongbox-commons +v:1.0");
        assertEquals("org.carlspring.strongbox:strongbox-commons:1.0 should not been deleted!", 2, search.size());

        repositoryIndexer.delete(asArtifactInfo(search));
        search = repositoryIndexer.search("org.carlspring.strongbox", "strongbox-commons", "1.0", null, null);

        assertEquals("org.carlspring.strongbox:strongbox-commons:1.0 should have been deleted!", 0, search.size());
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
