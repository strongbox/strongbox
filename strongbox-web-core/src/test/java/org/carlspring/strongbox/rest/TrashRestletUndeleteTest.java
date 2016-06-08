package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.context.RestletTestContext;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@RestletTestContext
public class TrashRestletUndeleteTest
        extends TestCaseWithArtifactGeneration
{

    private final static File BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory()).getAbsoluteFile();

    private static final String STORAGE = "storage0";

    private static final String REPOSITORY_WITH_TRASH = "releases-with-trash";

    private final static String REPOSITORY_WITH_TRASH_BASEDIR = BASEDIR.getAbsolutePath() +
                                                                "/storages/" + STORAGE + "/" + REPOSITORY_WITH_TRASH;

    private RestClient client = new RestClient();

    private static final File ARTIFACT_FILE_IN_TRASH = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/.trash/" +
                                                                "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                                "test-artifact-undelete-1.0.jar").getAbsoluteFile();


    @Before
    public void setUp()
            throws Exception
    {
        final String gavtc = "org.carlspring.strongbox.undelete:test-artifact-undelete::jar";

        System.out.println("REPOSITORY_WITH_TRASH_BASEDIR: " + REPOSITORY_WITH_TRASH_BASEDIR);
        System.out.println("BASEDIR.getAbsolutePath(): " + BASEDIR.getAbsolutePath());

        generateArtifact(REPOSITORY_WITH_TRASH_BASEDIR, gavtc, new String[]{ "1.0" });
        generateArtifact(BASEDIR.getAbsolutePath() + "/storages/" + STORAGE + "/releases", gavtc, new String[]{ "1.1" });

        // Delete the artifact (this one should get placed under the .trash)
        client.delete(STORAGE,
                      REPOSITORY_WITH_TRASH,
                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar");

        // Delete the artifact (this one shouldn't get placed under the .trash)
        client.delete(STORAGE,
                      "releases",
                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.1/test-artifact-undelete-1.1.jar");
    }

    @After
    public void tearDown()
            throws Exception
    {
        if (client != null)
        {
            client.close();
        }
    }

    @Test
    public void testForceDeleteArtifactNotAllowed()
            throws Exception
    {
        final String artifactPath = "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar";

        final File repositoryDir = new File(BASEDIR + "/storages/storage0/releases-with-trash/.trash");
        final File artifactFile = new File(repositoryDir, artifactPath);

        System.out.println("Artifact file: " + artifactFile.getAbsolutePath());

        assertTrue("Should have moved the artifact to the trash during a force delete operation, " +
                          "when allowsForceDeletion is not enabled!",
                          artifactFile.exists());
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForRepository()
            throws Exception
    {
        client.undelete(STORAGE,
                        REPOSITORY_WITH_TRASH,
                        "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar");

        File artifactFileRestoredFromTrash = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/" +
                                                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                      "test-artifact-undelete-1.0.jar").getAbsoluteFile();

        assertFalse("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!", ARTIFACT_FILE_IN_TRASH.exists());
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!", artifactFileRestoredFromTrash.exists());
    }

    @Test
    public void testUndeleteArtifactsForAllRepositories()
            throws Exception
    {
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                   ARTIFACT_FILE_IN_TRASH.getParentFile().exists());

        client.undeleteTrash();

        File artifactFileRestoredFromTrash = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/" +
                                                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                      "test-artifact-undelete-1.0.jar").getAbsoluteFile();

        assertFalse("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!", ARTIFACT_FILE_IN_TRASH.exists());
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH +
                   "' (" + artifactFileRestoredFromTrash.getAbsolutePath() + " does not exist)!",
                   artifactFileRestoredFromTrash.exists());
    }

}
