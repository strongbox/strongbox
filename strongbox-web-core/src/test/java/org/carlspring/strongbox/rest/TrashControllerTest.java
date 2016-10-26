package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.Assert.assertFalse;

/**
 * Created by yury on 8/27/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
public class TrashControllerTest
        extends RestAssuredBaseTest
{

    private static final File BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory()).getAbsoluteFile();
    private static final String STORAGE = "storage0";
    private static final String REPOSITORY_WITH_TRASH = "releases-with-trash";
    private static final String REPOSITORY_WITH_TRASH_BASEDIR = BASEDIR.getAbsolutePath() +
                                                                "/storages/" + STORAGE + "/" + REPOSITORY_WITH_TRASH;
    private static final File ARTIFACT_FILE_IN_TRASH = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/.trash/" +
                                                                "org/carlspring/strongbox/test-artifact-to-trash/1.0/" +
                                                                "test-artifact-to-trash-1.0.jar").getAbsoluteFile();

    @Before
    public void setUp()
            throws Exception
    {
        // remove release directory
        removeDir(new File(REPOSITORY_WITH_TRASH));
        removeDir(new File(REPOSITORY_WITH_TRASH_BASEDIR));

        final String gavtc = "org.carlspring.strongbox:test-artifact-to-trash::jar";

        logger.debug("REPOSITORY_WITH_TRASH_BASEDIR: " + REPOSITORY_WITH_TRASH_BASEDIR);
        logger.debug("BASEDIR.getAbsolutePath(): " + BASEDIR.getAbsolutePath());

        TestCaseWithArtifactGeneration.generateArtifact(REPOSITORY_WITH_TRASH_BASEDIR, gavtc, new String[]{ "1.0" });
        TestCaseWithArtifactGeneration.generateArtifact(
                BASEDIR.getAbsolutePath() + "/storages/" + STORAGE + "/releases", gavtc, new String[]{ "1.1" });

        // Delete the artifact (this one should get placed under the .trash)
        delete(STORAGE,
               REPOSITORY_WITH_TRASH,
               "org/carlspring/strongbox/test-artifact-to-trash/1.0/test-artifact-to-trash-1.0.jar",
               true);

        // Delete the artifact (this one shouldn't get placed under the .trash)
        delete(STORAGE,
               "releases",
               "org/carlspring/strongbox/test-artifact-to-trash/1.1/test-artifact-to-trash-1.1.jar",
               true);
    }

    @Test
    public void testForceDeleteArtifactNotAllowed()
            throws Exception
    {
        final String artifactPath = "org/carlspring/strongbox/test-artifact-to-trash/1.0/test-artifact-to-trash-1.0.jar";

        final File repositoryDir = new File(BASEDIR + "/storages/storage0/releases-with-trash/.trash");
        final File artifactFile = new File(repositoryDir, artifactPath);

        logger.debug("Artifact file: " + artifactFile.getAbsolutePath());

        Assert.assertTrue("Should have moved the artifact to the trash during a force delete operation, " +
                          "when allowsForceDeletion is not enabled!",
                          artifactFile.exists());
    }

    @Test
    public void testForceDeleteArtifactAllowed()
            throws Exception
    {
        final String artifactPath = "org/carlspring/strongbox/test-artifact-to-trash/1.1/test-artifact-to-trash-1.1.jar";

        final File repositoryTrashDir = new File(BASEDIR + "/storages/storage0/releases/.trash");
        final File repositoryDir = new File(BASEDIR + "/storages/storage0/releases/.trash");

        Assert.assertFalse("Failed to delete artifact during a force delete operation!",
                           new File(repositoryTrashDir, artifactPath).exists());
        Assert.assertFalse("Failed to delete artifact during a force delete operation!",
                           new File(repositoryDir, artifactPath).exists());
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForRepository()
            throws Exception
    {

        String url = getContextBaseUrl() + "/trash/" + STORAGE + "/" + REPOSITORY_WITH_TRASH;


        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .when()
                .delete(url)
                .peek()
                .then()
                .statusCode(200);

        assertFalse("Failed to empty trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                    ARTIFACT_FILE_IN_TRASH.exists());
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForAllRepositories()
            throws Exception
    {

        String url = getContextBaseUrl() + "/trash";


        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .when()
                .delete(url)
                .peek()
                .then()
                .statusCode(200);

        assertFalse("Failed to empty trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                    ARTIFACT_FILE_IN_TRASH.exists());
    }

    public void delete(String storageId,
                       String repositoryId,
                       String path,
                       boolean force)
            throws ArtifactOperationException
    {
        @SuppressWarnings("ConstantConditions")
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId;

        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .params("path", path, "force", force)
                .when()
                .delete(url)
                .peek()
                .then()
                .statusCode(200);
    }

    public static class SpringConfig
    {

    }

}
