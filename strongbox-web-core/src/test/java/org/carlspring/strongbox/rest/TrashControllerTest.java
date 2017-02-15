package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
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
public class TrashControllerTest
        extends RestAssuredBaseTest
{

    private static final File BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory()).getAbsoluteFile();

    private static final String REPOSITORY_WITH_TRASH = "releases-with-trash";

    private static final String REPOSITORY_WITH_FORCE_DELETE = "releases-with-force-delete";

    private static final String REPOSITORY_WITH_TRASH_BASEDIR = BASEDIR.getAbsolutePath() +
                                                                "/storages/" + STORAGE0 + "/" + REPOSITORY_WITH_TRASH;

    private static final File ARTIFACT_FILE_IN_TRASH = new File(REPOSITORY_WITH_TRASH_BASEDIR + "/.trash/" +
                                                                "org/carlspring/strongbox/test-artifact-to-trash/1.0/" +
                                                                "test-artifact-to-trash-1.0.jar").getAbsoluteFile();

    @Autowired
    private ConfigurationManager configurationManager;


    @Override
    public void init()
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
                             "org.carlspring.strongbox:test-artifact-to-trash:1.0");

            // Notes:
            // - Used by testForceDeleteArtifactAllowed()
            // - Forced deletions are allowed
            Repository repositoryWithForceDeletions = new Repository(REPOSITORY_WITH_FORCE_DELETE);
            repositoryWithForceDeletions.setStorage(storage);
            repositoryWithForceDeletions.setAllowsForceDeletion(false);
            repositoryWithForceDeletions.setIndexingEnabled(false);

            createRepository(repositoryWithForceDeletions);

            generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_FORCE_DELETE).getAbsolutePath(),
                             "org.carlspring.strongbox:test-artifact-to-trash:1.1");

            // Delete the artifact (this one should get placed under the .trash)
            client.delete(STORAGE0,
                          REPOSITORY_WITH_TRASH,
                          "org/carlspring/strongbox/test-artifact-to-trash/1.0/test-artifact-to-trash-1.0.jar",
                          true);

            // Delete the artifact (this one shouldn't get placed under the .trash)
            client.delete(STORAGE0,
                          REPOSITORY_WITH_FORCE_DELETE,
                          "org/carlspring/strongbox/test-artifact-to-trash/1.1/test-artifact-to-trash-1.1.jar",
                          true);
        }
        catch (Exception e)
        {
            throw new AssertionError("Unable to initialize test", e);
        }
    }

    @Override
    public Map<String, String> getRepositoriesToClean()
    {
        Map<String, String> repositories = new LinkedHashMap<>();
        repositories.put(STORAGE0, REPOSITORY_WITH_TRASH);
        repositories.put(STORAGE0, REPOSITORY_WITH_FORCE_DELETE);

        return repositories;
    }

    @Test
    public void testForceDeleteArtifactNotAllowed()
            throws Exception
    {
        final String artifactPath = "org/carlspring/strongbox/test-artifact-to-trash/1.0/test-artifact-to-trash-1.0.jar";

        final File repositoryDir = new File(BASEDIR + "/storages/storage0/" + REPOSITORY_WITH_TRASH + "/.trash");
        final File repositoryIndexDir = new File(BASEDIR + "/storages/storage0/" + REPOSITORY_WITH_TRASH + "/.index");
        final File artifactFile = new File(repositoryDir, artifactPath);

        logger.debug("Artifact file: " + artifactFile.getAbsolutePath());

        assertTrue("Should have moved the artifact to the trash during a force delete operation, " +
                   "when allowsForceDeletion is not enabled!",
                   artifactFile.exists());

        assertTrue("Should not have deleted .index directory!",
                   repositoryIndexDir.exists());
    }

    @Test
    public void testForceDeleteArtifactAllowed()
            throws Exception
    {
        final String artifactPath = "org/carlspring/strongbox/test-artifact-to-trash/1.1/test-artifact-to-trash-1.1.jar";

        final File repositoryTrashDir = new File(BASEDIR + "/storages/storage0/" + REPOSITORY_WITH_FORCE_DELETE + "/.trash");
        final File repositoryDir = new File(BASEDIR + "/storages/storage0/" + REPOSITORY_WITH_FORCE_DELETE + "/.trash");

        assertFalse("Failed to delete artifact during a force delete operation!",
                    new File(repositoryTrashDir, artifactPath).exists());
        assertFalse("Failed to delete artifact during a force delete operation!",
                    new File(repositoryDir, artifactPath).exists());
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForRepository()
            throws Exception
    {
        String url = getContextBaseUrl() + "/trash/" + STORAGE0 + "/" + REPOSITORY_WITH_TRASH;

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
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

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .delete(url)
               .peek()
               .then()
               .statusCode(200);

        assertFalse("Failed to empty trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                    ARTIFACT_FILE_IN_TRASH.exists());
    }

}
