package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
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

    private static final String REPOSITORY_WITH_TRASH = "tcut-releases-with-trash";

    private static final String REPOSITORY_RELEASES = "tcut-releases";

    private static final String REPOSITORY_WITH_TRASH_BASEDIR = BASEDIR.getAbsolutePath() +
                                                                "/storages/" + STORAGE + "/" + REPOSITORY_WITH_TRASH;

    private static final File ARTIFACT_FILE_IN_TRASH = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/.trash/" +
                                                                "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                                "test-artifact-undelete-1.0.jar").getAbsoluteFile();

    @Autowired
    private ConfigurationManager configurationManager;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Before
    public void setUp()
            throws Exception
    {
        super.init();

        try
        {
            Storage storage = configurationManager.getConfiguration().getStorage(STORAGE0);

            // Notes:
            // - Used by testForceDeleteArtifactNotAllowed()
            // - Forced deletions are not allowed
            // - Has enabled trash
            Repository repositoryWithTrash = new Repository(REPOSITORY_WITH_TRASH);
            repositoryWithTrash.setStorage(storage);
            repositoryWithTrash.setAllowsForceDeletion(false);
            repositoryWithTrash.setTrashEnabled(true);
            repositoryWithTrash.setIndexingEnabled(false);

            createRepository(repositoryWithTrash);

            generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_TRASH).getAbsolutePath(),
                             "org.carlspring.strongbox.undelete:test-artifact-undelete",
                             new String[] { "1.0" });

            // Notes:
            // - Used by testForceDeleteArtifactAllowed()
            // - Forced deletions are allowed
            Repository repositoryReleases = new Repository(REPOSITORY_RELEASES);
            repositoryReleases.setStorage(storage);
            repositoryReleases.setAllowsForceDeletion(false);
            repositoryReleases.setIndexingEnabled(false);

            createRepository(repositoryReleases);

            generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES).getAbsolutePath(),
                             "org.carlspring.strongbox:test-artifact-to-trash:1.1");

            // Delete the artifact (this one should get placed under the .trash)
            client.delete(STORAGE,
                          REPOSITORY_WITH_TRASH,
                          "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar");


            // Delete the artifact (this one shouldn't get placed under the .trash)
            client.delete(STORAGE,
                          REPOSITORY_RELEASES,
                          "org/carlspring/strongbox/undelete/test-artifact-undelete/1.1/test-artifact-undelete-1.1.jar");
        }
        catch (Exception e)
        {
            throw new AssertionError("Unable to prepare test.", e);
        }
    }

    public static Map<String, String> getRepositoriesToClean()
    {
        Map<String, String> repositories = new LinkedHashMap<>();
        repositories.put(STORAGE0, REPOSITORY_WITH_TRASH);
        repositories.put(STORAGE0, REPOSITORY_RELEASES);

        return repositories;
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
