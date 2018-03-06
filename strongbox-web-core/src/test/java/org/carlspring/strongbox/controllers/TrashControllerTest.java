package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.http.HttpHeaders;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Martin Todorov
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class TrashControllerTest
        extends MavenRestAssuredBaseTest
{

    private static final String BASEDIR = Paths.get(
            ConfigurationResourceResolver.getVaultDirectory()).toAbsolutePath().toString();

    private static final String REPOSITORY_WITH_TRASH = "tct-releases-with-trash";

    private static final String REPOSITORY_WITH_FORCE_DELETE = "tct-releases-with-force-delete";

    private static final String REPOSITORY_WITH_TRASH_BASEDIR = BASEDIR +
                                                                "/storages/" + STORAGE0 + "/" + REPOSITORY_WITH_TRASH;

    private static final Path ARTIFACT_FILE_IN_TRASH = Paths.get(REPOSITORY_WITH_TRASH_BASEDIR + "/.trash/" +
                                                                 "org/carlspring/strongbox/test-artifact-to-trash/1.0/" +
                                                                 "test-artifact-to-trash-1.0.jar");

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;


    @BeforeClass
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Before
    public void initialize()
            throws Exception
    {
        super.init();

        // Notes:
        // - Used by testForceDeleteArtifactNotAllowed()
        // - Forced deletions are not allowed
        // - Has enabled trash
        MavenRepositoryConfiguration mavenRepositoryConfiguration = new MavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

        Repository repositoryWithTrash = mavenRepositoryFactory.createRepository(STORAGE0, REPOSITORY_WITH_TRASH);
        repositoryWithTrash.setAllowsForceDeletion(false);
        repositoryWithTrash.setTrashEnabled(true);
        repositoryWithTrash.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(repositoryWithTrash);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_TRASH).getAbsolutePath(),
                         "org.carlspring.strongbox:test-artifact-to-trash:1.0");

        // Notes:
        // - Used by testForceDeleteArtifactAllowed()
        // - Forced deletions are allowed
        Repository repositoryWithForceDeletions = mavenRepositoryFactory.createRepository(STORAGE0,
                                                                                          REPOSITORY_WITH_FORCE_DELETE);
        repositoryWithForceDeletions.setAllowsForceDeletion(false);
        repositoryWithForceDeletions.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(repositoryWithForceDeletions);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_FORCE_DELETE).getAbsolutePath(),
                         "org.carlspring.strongbox:test-artifact-to-trash:1.1");
    }

    @After
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<Repository> getRepositoriesToClean()
    {
        Set<Repository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_WITH_TRASH));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_WITH_FORCE_DELETE));

        return repositories;
    }

    @Test
    public void testForceDeleteArtifactNotAllowed()
            throws Exception
    {
        final String artifactPath = "org/carlspring/strongbox/test-artifact-to-trash/1.0/test-artifact-to-trash-1.0.jar";

        // Delete the artifact (this one should get placed under the .trash)
        client.delete(STORAGE0, REPOSITORY_WITH_TRASH, artifactPath, false);

        final Path repositoryDir = Paths.get(BASEDIR + "/storages/" + STORAGE0 + "/" + REPOSITORY_WITH_TRASH + "/.trash");
        final Path repositoryIndexDir = Paths.get(BASEDIR + "/storages/" + STORAGE0 + "/" + REPOSITORY_WITH_TRASH + "/.index");
        final Path artifactFile = repositoryDir.resolve(artifactPath);

        logger.debug("Artifact file: " + artifactFile.toAbsolutePath());

        assertTrue("Should have moved the artifact to the trash during a force delete operation, " +
                   "when allowsForceDeletion is not enabled!",
                   Files.exists(artifactFile));

        assertTrue("Should not have deleted .index directory!",
                   Files.exists(repositoryIndexDir));
    }

    @Test
    public void testForceDeleteArtifactAllowed()
            throws Exception
    {
        final String artifactPath = "org/carlspring/strongbox/test-artifact-to-trash/1.1/test-artifact-to-trash-1.1.jar";

        // Delete the artifact (this one shouldn't get placed under the .trash)
        client.delete(STORAGE0, REPOSITORY_WITH_FORCE_DELETE, artifactPath, true);

        final Path repositoryTrashDir = Paths.get(BASEDIR + "/storages/" + STORAGE0 + "/" +
                                                  REPOSITORY_WITH_FORCE_DELETE + "/.trash");

        final Path repositoryDir = Paths.get(BASEDIR + "/storages/" + STORAGE0 + "/" +
                                             REPOSITORY_WITH_FORCE_DELETE + "/.trash");

        assertFalse("Failed to delete artifact during a force delete operation!",
                    Files.exists(repositoryTrashDir.resolve(artifactPath)));
        assertFalse("Failed to delete artifact during a force delete operation!",
                    Files.exists(repositoryDir.resolve(artifactPath)));
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForRepositoryWithTextAcceptHeader()
    {
        String url = getContextBaseUrl() + "/api/trash/" + STORAGE0 + "/" + REPOSITORY_WITH_TRASH;

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .when()
               .delete(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(
                       "The trash for '" + STORAGE0 + ":" + REPOSITORY_WITH_TRASH + "' was removed successfully."));

        assertFalse("Failed to empty trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                    Files.exists(ARTIFACT_FILE_IN_TRASH));
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForRepositoryWithJsonAcceptHeader()
    {
        String url = getContextBaseUrl() + "/api/trash/" + STORAGE0 + "/" + REPOSITORY_WITH_TRASH;

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo(
                       "The trash for '" + STORAGE0 + ":" + REPOSITORY_WITH_TRASH + "' was removed successfully."));

        assertFalse("Failed to empty trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                    Files.exists(ARTIFACT_FILE_IN_TRASH));
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForAllRepositoriesWithTextAcceptHeader()
    {
        String url = getContextBaseUrl() + "/api/trash";

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .when()
               .delete(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo("The trash for all repositories was successfully removed."));

        assertFalse("Failed to empty trash for repository '" + REPOSITORY_WITH_TRASH + "'!",
                    Files.exists(ARTIFACT_FILE_IN_TRASH));
    }

    @Test
    public void testDeleteArtifactAndEmptyTrashForAllRepositoriesWithJsonAcceptHeader()
    {
        String url = getContextBaseUrl() + "/api/trash";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("The trash for all repositories was successfully removed."));

        assertFalse("Failed to empty trash for all repositories", Files.exists(ARTIFACT_FILE_IN_TRASH));
    }

}
