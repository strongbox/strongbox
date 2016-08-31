package org.carlspring.strongbox.rest;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by yury on 8/30/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
public class TrashControllerUndeleteTest
        extends BackendBaseTest {

    private static final File BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory()).getAbsoluteFile();

    private static final String STORAGE = "storage0";

    private static final String REPOSITORY_WITH_TRASH = "releases-with-trash";

    private static final String REPOSITORY_WITH_TRASH_BASEDIR = BASEDIR.getAbsolutePath() +
            "/storages/" + STORAGE + "/" + REPOSITORY_WITH_TRASH;

    private static final File ARTIFACT_FILE_IN_TRASH = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/.trash/" +
            "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
            "test-artifact-undelete-1.0.jar").getAbsoluteFile();


    @Before
    public void setUp()
            throws Exception {
        // remove release directory
        removeDir(new File(REPOSITORY_WITH_TRASH));
        removeDir(new File(REPOSITORY_WITH_TRASH_BASEDIR));

        final String gavtc = "org.carlspring.strongbox.undelete:test-artifact-undelete::jar";

        System.out.println("REPOSITORY_WITH_TRASH_BASEDIR: " + REPOSITORY_WITH_TRASH_BASEDIR);
        System.out.println("BASEDIR.getAbsolutePath(): " + BASEDIR.getAbsolutePath());

        TestCaseWithArtifactGeneration.generateArtifact(REPOSITORY_WITH_TRASH_BASEDIR, gavtc, new String[]{"1.0"});
        TestCaseWithArtifactGeneration.generateArtifact(BASEDIR.getAbsolutePath() + "/storages/" + STORAGE + "/releases", gavtc, new String[]{"1.1"});

        // Delete the artifact (this one should get placed under the .trash)
        delete(STORAGE,
                REPOSITORY_WITH_TRASH,
                "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar");


        // Delete the artifact (this one shouldn't get placed under the .trash)
        delete(STORAGE,
                "releases",
                "org/carlspring/strongbox/undelete/test-artifact-undelete/1.1/test-artifact-undelete-1.1.jar");

    }


    private void removeDir(File dir) {

        if (dir == null) {
            return;
        }

        System.out.println("Removing directory " + dir.getAbsolutePath());

        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    removeDir(file);
                }
            }
        } else {
            boolean res = dir.delete();
            System.out.println("Remove " + dir.getAbsolutePath() + " " + res);
        }
    }


    @Test
    public void testForceDeleteArtifactNotAllowed()
            throws Exception {
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
            throws Exception {
        String url = getContextBaseUrl() + "/trash/" + STORAGE + "/" + REPOSITORY_WITH_TRASH;

        RestAssuredMockMvc.given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .param("path", "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar")
                .when()
                .post(url)
                .peek()
                .then()
                .statusCode(200);

        File artifactFileRestoredFromTrash = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/" +
                "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                "test-artifact-undelete-1.0.jar").getAbsoluteFile();

        assertFalse("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!", ARTIFACT_FILE_IN_TRASH.exists());
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!", artifactFileRestoredFromTrash.exists());
    }

    @Test
    public void testUndeleteArtifactsForAllRepositories()
            throws Exception {
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                ARTIFACT_FILE_IN_TRASH.getParentFile().exists());

        String url = getContextBaseUrl() + "/trash";

        RestAssuredMockMvc.given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .when()
                .post(url)
                .peek()
                .then()
                .statusCode(200);

        File artifactFileRestoredFromTrash = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/" +
                "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                "test-artifact-undelete-1.0.jar").getAbsoluteFile();

        assertFalse("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!", ARTIFACT_FILE_IN_TRASH.exists());
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH +
                        "' (" + artifactFileRestoredFromTrash.getAbsolutePath() + " does not exist)!",
                artifactFileRestoredFromTrash.exists());
    }


    public void delete(String storageId,
                       String repositoryId,
                       String path)
            throws ArtifactOperationException {
        delete(storageId, repositoryId, path, false);
    }

    public void delete(String storageId,
                       String repositoryId,
                       String path,
                       boolean force)
            throws ArtifactOperationException {
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId;

        RestAssuredMockMvc.given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .params("path", path, "force", force)
                .when()
                .delete(url)
                .peek()
                .then()
                .statusCode(200);
    }

}
