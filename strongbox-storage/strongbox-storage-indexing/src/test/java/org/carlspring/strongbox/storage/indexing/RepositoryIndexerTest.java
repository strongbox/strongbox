package org.carlspring.strongbox.storage.indexing;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class RepositoryIndexerTest
        extends TestCaseWithArtifactGenerationWithIndexing
{
    private static final Logger logger = LoggerFactory.getLogger(RepositoryIndexerTest.class);

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases");

    private static final File INDEX_DIR = new File(REPOSITORY_BASEDIR, ".index");


    @Before
    public void init()
            throws NoSuchAlgorithmException,
            XmlPullParserException,
            IOException,
            ArtifactOperationException
    {
        //noinspection ResultOfMethodCallIgnored
        INDEX_DIR.mkdirs();

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-commons:1.0:jar");
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-commons:1.1:jar");
        Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-commons:1.2:jar");

        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact1);
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact2);
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), artifact3);
    }

    @Test
    public void testIndex() throws Exception
    {
        final RepositoryIndexer repositoryIndexer = getRepositoryIndexManager().getRepositoryIndex("storage0:releases");

        final int x = repositoryIndexer.index(new File("org/carlspring/strongbox/strongbox-commons"));

        Assert.assertEquals("6 artifacts expected!",
                            6,  // one is jar another pom, both would be added into the same Lucene document
                            x);

        Set<SearchResult> search = repositoryIndexer.search("org.carlspring.strongbox",
                                                            "strongbox-commons",
                                                            null,
                                                            null,
                                                            null);

        Assert.assertEquals("Only three versions of the strongbox-commons artifact were expected!", 3, search.size());

        search = repositoryIndexer.search("org.carlspring.strongbox", "strongbox-commons", "1.0", "jar", null);
        Assert.assertEquals("org.carlspring.strongbox:strongbox-commons:1.0 should have been deleted!", 1, search.size());

        search = repositoryIndexer.search("+g:org.carlspring.strongbox +a:strongbox-commons +v:1.0");
        Assert.assertEquals("org.carlspring.strongbox:strongbox-commons:1.0 should have been deleted!", 1, search.size());

        repositoryIndexer.delete(asArtifactInfo(search));
        search = repositoryIndexer.search("org.carlspring.strongbox", "strongbox-commons", "1.0", null, null);

        Assert.assertEquals("org.carlspring.strongbox:strongbox-commons:1.0 should have been deleted!", 0, search.size());
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
