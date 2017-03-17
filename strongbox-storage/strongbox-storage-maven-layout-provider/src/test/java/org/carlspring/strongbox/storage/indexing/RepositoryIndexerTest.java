package org.carlspring.strongbox.storage.indexing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.PreDestroy;
import javax.xml.bind.JAXBException;

import org.apache.maven.index.ArtifactInfo;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationAndIndexing;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class RepositoryIndexerTest
        extends TestCaseWithArtifactGenerationAndIndexing
{

    public static final String REPOSITORY_RELEASES = "ri-releases";

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
                                      REPOSITORY_RELEASES,
                                      true,
                                      "org.carlspring.strongbox:strongbox-commons",
                                      "1.0", "1.1", "1.2");
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
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES));

        return repositories;
    }

    @Test
    public void testIndex() throws Exception
    {
        RepositoryIndexer repositoryIndexer = getRepositoryIndexManager().getRepositoryIndexer(STORAGE0 + ":" + REPOSITORY_RELEASES + ":local");

        int x = repositoryManagementService.reIndex(STORAGE0,
                                                    REPOSITORY_RELEASES,
                                                    "org/carlspring/strongbox/strongbox-commons");

        repositoryManagementService.pack(STORAGE0, REPOSITORY_RELEASES);

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
        assertEquals("org.carlspring.strongbox:strongbox-commons:1.0 should have been deleted!", 1, search.size());

        search = repositoryIndexer.search("+g:org.carlspring.strongbox +a:strongbox-commons +v:1.0");
        assertEquals("org.carlspring.strongbox:strongbox-commons:1.0 should have been deleted!", 1, search.size());

        repositoryIndexer.delete(asArtifactInfo(search));
        search = repositoryIndexer.search("org.carlspring.strongbox", "strongbox-commons", "1.0", null, null);

        assertEquals("org.carlspring.strongbox:strongbox-commons:1.0 should have been deleted!", 0, search.size());
    }

    private Collection<ArtifactInfo> asArtifactInfo(Set<SearchResult> results)
    {
        Collection<ArtifactInfo> artifactInfos = new LinkedHashSet<>();
        for (SearchResult result : results)
        {
            artifactInfos.add(new ArtifactInfo(result.getRepositoryId(),
                    ((MavenArtifactCoordinates) result.getArtifactCoordinates()).getGroupId(),
                    ((MavenArtifactCoordinates) result.getArtifactCoordinates()).getArtifactId(),
                    ((MavenArtifactCoordinates) result.getArtifactCoordinates()).getVersion(),
                    ((MavenArtifactCoordinates) result.getArtifactCoordinates()).getClassifier(),
                    ((MavenArtifactCoordinates) result.getArtifactCoordinates()).getExtension()));
        }

        return artifactInfos;
    }

}
