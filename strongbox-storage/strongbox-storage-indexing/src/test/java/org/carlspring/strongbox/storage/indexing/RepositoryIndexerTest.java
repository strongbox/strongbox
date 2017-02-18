package org.carlspring.strongbox.storage.indexing;

import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.maven.index.ArtifactInfo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public class RepositoryIndexerTest
        extends TestCaseWithArtifactGenerationWithIndexing
{

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
                                      "repository-indexer-test-releases",
                                      true,
                                      "org.carlspring.strongbox:strongbox-commons",
                                      "1.0", "1.1", "1.2");
    }

    public static Map<String, String> getRepositoriesToClean()
    {
        Map<String, String> repositories = new LinkedHashMap<>();
        repositories.put(STORAGE0, "repository-indexer-test-releases");

        return repositories;
    }

    @Test
    public void testIndex() throws Exception
    {
        final RepositoryIndexer repositoryIndexer = getRepositoryIndexManager().getRepositoryIndexer("storage0:repository-indexer-test-releases:local");

        final int x = repositoryManagementService.reIndex(STORAGE0,
                                                          "repository-indexer-test-releases",
                                                          "org/carlspring/strongbox/strongbox-commons");

        repositoryManagementService.pack(STORAGE0, "repository-indexer-test-releases");

        File repositoryBasedir = getRepositoryBasedir(STORAGE0, "repository-indexer-test-releases");

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
                                               result.getGroupId(),
                                               result.getArtifactId(),
                                               result.getVersion(),
                                               result.getClassifier(),
                                               result.getExtension()));
        }

        return artifactInfos;
    }

}
