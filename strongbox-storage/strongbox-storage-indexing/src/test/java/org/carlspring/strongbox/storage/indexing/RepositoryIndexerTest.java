package org.carlspring.strongbox.storage.indexing;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.index.ArtifactInfo;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class RepositoryIndexerTest
{

    private static final File REPOSITORY_BASEDIR = new File("target/storages/storage0/releases");

    private static final File INDEX_DIR = new File(REPOSITORY_BASEDIR, ".index");

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;


    @Before
    public void init()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        //noinspection ResultOfMethodCallIgnored
        INDEX_DIR.mkdirs();

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-commons:1.0:jar");
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-commons:1.1:jar");
        Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:strongbox-commons:1.2:jar");

        ArtifactGenerator generator = new ArtifactGenerator(REPOSITORY_BASEDIR.getAbsolutePath());
        generator.generate(artifact1);
        generator.generate(artifact2);
        generator.generate(artifact3);
    }

    @Test
    public void testIndex() throws Exception
    {
        // final RepositoryIndexer i = new RepositoryIndexer("releases", REPOSITORY_BASEDIR, INDEX_DIR);
        final RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndex("storage0:releases");

        final int x = repositoryIndexer.index(new File("org/carlspring/strongbox/strongbox-commons"));

        Assert.assertEquals("6 artifacts expected!",
                            6,  // one is jar another pom, both would be added into the same Lucene document
                            x);

        Set<ArtifactInfo> search = repositoryIndexer.search("org.carlspring.strongbox", "strongbox-commons", null, null, null);
        for (final ArtifactInfo ai : search)
        {
            System.out.println(ai.groupId + " / " + ai.artifactId + " / " + ai.version + " / " + ai.description);
        }

        Assert.assertEquals("Only three versions of the strongbox-commons artifact were expected!", 3, search.size());

        repositoryIndexer.delete(search);
        search = repositoryIndexer.search("org.carlspring.strongbox", "strongbox-commons", "1.0", null, null);

        Assert.assertEquals("org.carlspring.strongbox:strongbox-commons:1.0 should have been deleted!", search.size(), 0);
    }

}
