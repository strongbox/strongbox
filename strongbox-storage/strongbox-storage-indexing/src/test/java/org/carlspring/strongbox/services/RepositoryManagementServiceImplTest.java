package org.carlspring.strongbox.services;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;
import org.carlspring.strongbox.storage.indexing.SearchRequest;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGenerationWithIndexing;

import java.io.File;
import java.io.IOException;

import org.apache.maven.artifact.Artifact;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class RepositoryManagementServiceImplTest
        extends TestCaseWithArtifactGenerationWithIndexing
{
    private static final Logger logger = LoggerFactory.getLogger(RepositoryManagementServiceImplTest.class);

    private static final String REPOSITORY_ID = "releases";

    private static final String STORAGES_BASEDIR = ConfigurationResourceResolver.getVaultDirectory() + "/storages";

    private static final File REPOSITORY_BASEDIR = new File(STORAGES_BASEDIR + "/storage0/" + REPOSITORY_ID);

    @Autowired
    private ArtifactSearchService artifactSearchService;

    @Test
    public void testCreateRepository()
            throws IOException
    {
        getRepositoryManagementService().createRepository("storage0", "test-releases1");

        assertTrue("Failed to create repository '" + REPOSITORY_ID + "'!", REPOSITORY_BASEDIR.exists());
    }

    @Test
    public void testCreateAndDelete()
            throws Exception
    {
        String storageId = "storage0";
        String repositoryId = "foo-snapshots";

        File basedir = new File(STORAGES_BASEDIR + "/" + storageId);
        File repositoryDir = new File(basedir, repositoryId);

        getRepositoryManagementService().createRepository(storageId, repositoryId);

        assertTrue("Failed to create the repository \"" + repositoryDir.getAbsolutePath() + "\"!", repositoryDir.exists());

        getRepositoryIndexManager().closeIndexer(storageId + ":" + repositoryId);

        getRepositoryManagementService().removeRepository("storage0", repositoryId);

        assertFalse("Failed to remove the repository!", repositoryDir.exists());
    }

    @Test
    public void testMerge()
            throws Exception
    {
        String repositoryId1 = "test-releases-merge-1";
        String repositoryId2 = "test-releases-merge-2";

        getRepositoryManagementService().createRepository("storage0", repositoryId1);
        getRepositoryManagementService().createRepository("storage0", repositoryId2);

        String gavtc = "org.carlspring.strongbox:strongbox-utils::jar";

        File repo1 = new File(STORAGES_BASEDIR + "/storage0/" + repositoryId1);
        File repo2 = new File(STORAGES_BASEDIR + "/storage0/" + repositoryId2);

        File artifactFile1 = new File(repo1, "org/carlspring/strongbox/strongbox-utils/6.2.2/strongbox-utils-6.2.2.jar");
        File artifactFile2 = new File(repo2, "org/carlspring/strongbox/strongbox-utils/6.2.2-SNAPSHOT/strongbox-utils-6.2.2-SNAPSHOT.jar");

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAV("org.carlspring.strongbox:strongbox-utils:6.2.2:jar");
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAV("org.carlspring.strongbox:strongbox-utils:6.2.2-SNAPSHOT:jar");

        generateArtifact(repo1.getAbsolutePath(), gavtc, new String[] { "6.2.2" });
        generateArtifact(repo2.getAbsolutePath(), gavtc, new String[] { "6.2.2-SNAPSHOT" });

        RepositoryIndexer indexer1 = getRepositoryIndexManager().getRepositoryIndex("storage0:test-releases-merge-1");
        RepositoryIndexer indexer2 = getRepositoryIndexManager().getRepositoryIndex("storage0:test-releases-merge-2");

        indexer1.addArtifactToIndex(repositoryId1, artifactFile1, artifact1);
        indexer2.addArtifactToIndex(repositoryId2, artifactFile2, artifact2);

        SearchRequest request = new SearchRequest("storage0",
                                                  repositoryId1,
                                                  "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.2 +p:jar");

        assertTrue(artifactSearchService.contains(request));

        request = new SearchRequest("storage0",
                                    repositoryId1,
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.2-SNAPSHOT +p:jar");

        assertFalse(artifactSearchService.contains(request));

        getRepositoryManagementService().mergeRepositoryIndex("storage0", repositoryId2, "storage0", repositoryId1);

        request = new SearchRequest("storage0",
                                    repositoryId1,
                                    "+g:org.carlspring.strongbox +a:strongbox-utils +v:6.2.2-SNAPSHOT +p:jar");

        assertTrue("Failed to merge!", artifactSearchService.contains(request));
    }

}
