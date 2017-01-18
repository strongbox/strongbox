package org.carlspring.strongbox.storage.indexing;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.config.ClientConfig;
import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.config.IndexResourceFetcherWithRestAssuredConfig;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.config.StorageIndexingConfig;
import org.carlspring.strongbox.config.UsersConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.index.ArtifactInfo;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(classes = { CommonConfig.class,
                                  // StrongboxSecurityConfig.class,
                                  StorageIndexingConfig.class,
                                  StorageApiConfig.class,
                                  UsersConfig.class,
                                  // SecurityConfig.class,
                                  ClientConfig.class,
                                  IndexResourceFetcherWithRestAssuredConfig.class,
                                  StorageIndexingConfig.class })
@RunWith(SpringJUnit4ClassRunner.class)
public class RepositoryIndexerTest
        extends RestAssuredBaseTest
{

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases");

    private static final File INDEX_DIR = new File(REPOSITORY_BASEDIR, ".index");

    @Autowired
    private RepositoryManagementService repositoryManagementService;

    TestCaseWithArtifactGenerationWithIndexing testCase = new TestCaseWithArtifactGenerationWithIndexing();


    @Before
    public void setUp()
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        //noinspection ResultOfMethodCallIgnored
        INDEX_DIR.mkdirs();

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-commons:1.0:jar");
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-commons:1.1:jar");
        Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-commons:1.2:jar");

        testCase.generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact1);
        testCase.generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact2);
        testCase.generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact3);
    }

    @Test
    public void testIndex() throws Exception
    {
        final RepositoryIndexer repositoryIndexer = testCase.getRepositoryIndexManager().getRepositoryIndex("storage0:releases");

        final int x = repositoryManagementService.reIndex("storage0", "releases", "org/carlspring/strongbox/strongbox-commons");

        repositoryManagementService.pack("storage0", "releases");

        assertTrue("Failed to pack index!", new File(REPOSITORY_BASEDIR.getAbsolutePath(), ".index/nexus-maven-repository-index.gz").exists());
        assertTrue("Failed to pack index!", new File(REPOSITORY_BASEDIR.getAbsolutePath(), ".index/nexus-maven-repository-index-packer.properties").exists());

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
