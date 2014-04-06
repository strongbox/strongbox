package org.carlspring.strongbox.storage.indexing;

import org.apache.maven.index.ArtifactInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Set;

public class LocalRepositoryIndexerTest
{
    final File indexDir = new File("target/test-idx");

    @Before
    public void init()
    {
        indexDir.mkdirs();
    }

    @Test
    public void testIndex() throws Exception
    {
        final LocalRepositoryIndexer i = new LocalRepositoryIndexer(
                "test", new File(System.getProperty("user.home"), ".m2/repository"), indexDir);
        try
        {
            final int x = i.index(new File("antlr"));
            Assert.assertEquals("two artifacts expected", x,
                    2); // one is jar another pom, both would be added into the same Lucene document

            Set<ArtifactInfo> search = i.search("antlr", "antlr", null);
            for (final ArtifactInfo ai : search)
            {
                System.out.println(ai.groupId + " / " + ai.artifactId + " / " + ai.version + " / " + ai.description);
            }
            Assert.assertEquals("only one antlr artifact", search.size(), 1);

            i.delete(search);
            search = i.search("antlr", "antlr", null);
            Assert.assertEquals("antlr should be deleted", search.size(), 0);
        }
        finally
        {
            i.close(false);
        }
    }
}