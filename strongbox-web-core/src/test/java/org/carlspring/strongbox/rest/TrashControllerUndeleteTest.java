package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration.generateArtifact;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Alex Oreshkevich
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class TrashControllerUndeleteTest
        extends RestAssuredBaseTest
{

    private static final File BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory()).getAbsoluteFile();

    private static final String STORAGE = "storage0";

    private static final String REPOSITORY_WITH_TRASH = "releases-with-trash";

    private static final String REPOSITORY_WITH_TRASH_BASEDIR = BASEDIR.getAbsolutePath() +
                                                                "/storages/" + STORAGE + "/" + REPOSITORY_WITH_TRASH;

    private static final File ARTIFACT_FILE_IN_TRASH = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/.trash/" +
                                                                "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                                "test-artifact-undelete-1.0.jar").getAbsoluteFile();

    @Override
    public void init()
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        super.init();

        // remove release directory
        removeDir(new File(REPOSITORY_WITH_TRASH, "org"));
        removeDir(new File(REPOSITORY_WITH_TRASH, "com"));
        removeDir(new File(REPOSITORY_WITH_TRASH, ".trash/org"));
        removeDir(new File(REPOSITORY_WITH_TRASH, ".trash/com"));

        try
        {
            final String gavtc = "org.carlspring.strongbox.undelete:test-artifact-undelete::jar";

            logger.debug("REPOSITORY_WITH_TRASH_BASEDIR: " + REPOSITORY_WITH_TRASH_BASEDIR);
            logger.debug("BASEDIR.getAbsolutePath(): " + BASEDIR.getAbsolutePath());

            generateArtifact(REPOSITORY_WITH_TRASH_BASEDIR, gavtc, "1.0");
            generateArtifact(BASEDIR.getAbsolutePath() + "/storages/" + STORAGE + "/releases", gavtc, "1.1");

            // Delete the artifact (this one should get placed under the .trash)
            client.delete(STORAGE,
                          REPOSITORY_WITH_TRASH,
                          "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar");


            // Delete the artifact (this one shouldn't get placed under the .trash)
            client.delete(STORAGE,
                          "releases",
                          "org/carlspring/strongbox/undelete/test-artifact-undelete/1.1/test-artifact-undelete-1.1.jar");
        }
        catch (Exception e)
        {
            throw new AssertionError("Unable to prepare test.", e);
        }
    }

    @Test
    public void testForceDeleteArtifactNotAllowed()
            throws Exception
    {
        final String artifactPath = "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar";

        final File repositoryDir = new File(BASEDIR + "/storages/storage0/releases-with-trash/.trash");
        final File artifactFile = new File(repositoryDir, artifactPath);

        logger.debug("Artifact file: " + artifactFile.getAbsolutePath());

        assertTrue("Should have moved the artifact to the trash during a force delete operation, " +
                   "when allowsForceDeletion is not enabled!",
                   artifactFile.exists());
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForRepository()
            throws Exception
    {
        String url = getContextBaseUrl() + "/trash/" + STORAGE + "/" + REPOSITORY_WITH_TRASH;
        url += "/org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar";

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .post(url)
               .peek()
               .then()
               .statusCode(200);

        File artifactFileRestoredFromTrash = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/" +
                                                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                      "test-artifact-undelete-1.0.jar").getAbsoluteFile();

        assertFalse("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                    ARTIFACT_FILE_IN_TRASH.exists());
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                   artifactFileRestoredFromTrash.exists());
    }

    @Test
    public void testUndeleteArtifactsForAllRepositories()
            throws Exception
    {
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                   ARTIFACT_FILE_IN_TRASH.getParentFile().exists());

        String url = getContextBaseUrl() + "/trash";

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .post(url)
               .peek()
               .then()
               .statusCode(200);

        File artifactFileRestoredFromTrash = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/" +
                                                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                      "test-artifact-undelete-1.0.jar").getAbsoluteFile();

        assertFalse("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                    ARTIFACT_FILE_IN_TRASH.exists());
        assertTrue("Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH +
                   "' (" + artifactFileRestoredFromTrash.getAbsolutePath() + " does not exist)!",
                   artifactFileRestoredFromTrash.exists());
    }

}
