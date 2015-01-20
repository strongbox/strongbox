package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.generator.ArtifactGenerator;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.SearchRequest;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class RepositoryManagementServiceImplTest
{

    public static final String REPOSITORY_ID = "releases";

    private static final File REPOSITORY_BASEDIR = new File("target/storages/storage0/" + REPOSITORY_ID);

    private static final File INDEX_DIR = new File(REPOSITORY_BASEDIR, ".index");

    private static boolean INITIALIZED = false;

    @Autowired
    private RepositoryManagementService repositoryManagementService;

    @Autowired
    private ArtifactSearchService artifactSearchService;

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;


    @Test
    public void testCreateRepository()
            throws IOException
    {
        repositoryManagementService.createRepository("storage0", REPOSITORY_ID);

        Assert.assertTrue("Failed to create repository '" + REPOSITORY_ID  + "'!", REPOSITORY_BASEDIR.exists());
    }

    @Test
    public void testCreateAndDelete()
            throws Exception
    {
        final String storageBaseDir = "target/storages/storage0";
        File basedir = new File(storageBaseDir);
        File repositoryDir = new File(basedir, "foo-snapshots");

        repositoryManagementService.createRepository("storage0", "foo-snapshots");

        assertTrue("Failed to create the repository \"" + repositoryDir.getAbsolutePath() + "\"!", repositoryDir.exists());

        repositoryManagementService.removeRepository("storage0", "foo-snapshots");

        assertFalse("Failed to remove the repository!", repositoryDir.exists());
    }

    @Test
    public void testMerge()
            throws Exception
    {
        String gavtc = "org.carlspring.strongbox:strongbox-utils::jar";

        ArtifactGenerator generator = new ArtifactGenerator(REPOSITORY_BASEDIR.getAbsolutePath());
        generator.generate(gavtc, "6.0.1", "6.1.1", "6.2.1", "6.2.2-SNAPSHOT", "7.0", "7.1");

        final RepositoryIndexer repositoryIndexer = repositoryIndexManager.getRepositoryIndex("storage0:releases");
        final int x = repositoryIndexer.index(new File("org/carlspring/strongbox/strongbox-utils"));

        SearchRequest request = new SearchRequest("storage0",
                                                  "releases",
                                                  "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.1 +p:jar");

        Assert.assertTrue(artifactSearchService.contains(request));

        request = new SearchRequest("storage0",
                                    "releases-with-trash",
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.1 +p:jar");

        Assert.assertFalse(artifactSearchService.contains(request));

        repositoryManagementService.mergeRepositoryIndex("storage0", "releases", "storage0", "releases-with-trash");

        request = new SearchRequest("storage0",
                                    "releases-with-trash",
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.1 +p:jar");

        Assert.assertTrue(artifactSearchService.contains(request));
    }

}
