package org.carlspring.strongbox.controllers;

import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Martin Todorov
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class TrashControllerTest
        extends MavenRestAssuredBaseTest
{

    private static final String REPOSITORY_WITH_TRASH = "tct-releases-with-trash";

    private static final String REPOSITORY_WITH_FORCE_DELETE = "tct-releases-with-force-delete";

    private static final String ARTIFACT_FILE_IN_TRASH = "/.trash/" +
                                                         "org/carlspring/strongbox/test-artifact-to-trash/1.0/" +
                                                         "test-artifact-to-trash-1.0.jar";

    @Inject
    private MavenRepositoryFactory mavenRepositoryFactory;


    @BeforeAll
    public static void cleanUp()
            throws Exception
    {
        cleanUp(getRepositoriesToClean());
    }

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        // Notes:
        // - Used by testForceDeleteArtifactNotAllowed()
        // - Forced deletions are not allowed
        // - Has enabled trash
        MutableMavenRepositoryConfiguration mavenRepositoryConfiguration = new MutableMavenRepositoryConfiguration();
        mavenRepositoryConfiguration.setIndexingEnabled(false);

        MutableRepository repositoryWithTrash = mavenRepositoryFactory.createRepository(REPOSITORY_WITH_TRASH);
        repositoryWithTrash.setAllowsForceDeletion(false);
        repositoryWithTrash.setTrashEnabled(true);
        repositoryWithTrash.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(STORAGE0, repositoryWithTrash);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_TRASH).getAbsolutePath(),
                         "org.carlspring.strongbox:test-artifact-to-trash:1.0");

        // Notes:
        // - Used by testForceDeleteArtifactAllowed()
        // - Forced deletions are allowed
        MutableRepository repositoryWithForceDeletions = mavenRepositoryFactory.createRepository(REPOSITORY_WITH_FORCE_DELETE);
        repositoryWithForceDeletions.setAllowsForceDeletion(false);
        repositoryWithForceDeletions.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(STORAGE0, repositoryWithForceDeletions);

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_FORCE_DELETE).getAbsolutePath(),
                         "org.carlspring.strongbox:test-artifact-to-trash:1.1");
    }

    @AfterEach
    public void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<MutableRepository> getRepositoriesToClean()
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_WITH_TRASH, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_WITH_FORCE_DELETE, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testForceDeleteArtifactNotAllowed()
    {
        final String artifactPath = "org/carlspring/strongbox/test-artifact-to-trash/1.0/test-artifact-to-trash-1.0.jar";

        // Delete the artifact (this one should get placed under the .trash)
        client.delete(STORAGE0, REPOSITORY_WITH_TRASH, artifactPath, false);

        final Path repositoryDir = Paths.get(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_TRASH).getAbsolutePath() + "/.trash");

        final Path artifactFile = repositoryDir.resolve(artifactPath);

        logger.debug("Artifact file: " + artifactFile.toAbsolutePath());

        assertTrue(Files.exists(artifactFile),
                   "Should have moved the artifact to the trash during a force delete operation, " +
                           "when allowsForceDeletion is not enabled!");

        Assumptions.assumeTrue(repositoryIndexManager.isPresent());

        final Path repositoryIndexDir = Paths.get(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_TRASH) + "/.index");

        assertTrue(Files.exists(repositoryIndexDir), "Should not have deleted .index directory!");
    }

    @Test
    public void testForceDeleteArtifactAllowed()
    {
        final String artifactPath = "org/carlspring/strongbox/test-artifact-to-trash/1.1/test-artifact-to-trash-1.1.jar";

        // Delete the artifact (this one shouldn't get placed under the .trash)
        client.delete(STORAGE0, REPOSITORY_WITH_FORCE_DELETE, artifactPath, true);

        final Path repositoryTrashDir = Paths.get(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_FORCE_DELETE) + "/.trash");

        final Path repositoryDir = Paths.get(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_FORCE_DELETE) + "/" +
                                             REPOSITORY_WITH_FORCE_DELETE + "/.trash");

        assertFalse(Files.exists(repositoryTrashDir.resolve(artifactPath)),
                    "Failed to delete artifact during a force delete operation!");
        assertFalse(Files.exists(repositoryDir.resolve(artifactPath)),
                    "Failed to delete artifact during a force delete operation!");
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testDeleteArtifactAndEmptyTrashForRepository(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/api/trash/" + STORAGE0 + "/" + REPOSITORY_WITH_TRASH;

        ValidatableMockMvcResponse response = given().accept(acceptHeader)
                                                     .when()
                                                     .delete(url)
                                                     .peek()
                                                     .then()
                                                     .statusCode(HttpStatus.OK.value());

        String message = "The trash for '" + STORAGE0 + ":" + REPOSITORY_WITH_TRASH + "' was removed successfully.";
        validateResponseBody(response, acceptHeader, message);

        assertFalse(Files.exists(getPathToArtifactInTrash()),
                    "Failed to empty trash for repository '" + REPOSITORY_WITH_TRASH + "'!");
    }

    private Path getPathToArtifactInTrash()
    {
        return Paths.get(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_TRASH).getAbsolutePath() + "/" +
                         ARTIFACT_FILE_IN_TRASH);
    }


    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testDeleteArtifactAndEmptyTrashForAllRepositories(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/api/trash";

        ValidatableMockMvcResponse response = given().accept(acceptHeader)
                                                     .when()
                                                     .delete(url)
                                                     .peek()
                                                     .then()
                                                     .statusCode(HttpStatus.OK.value());

        String message = "The trash for all repositories was successfully removed.";
        validateResponseBody(response, acceptHeader, message);

        assertFalse(Files.exists(getPathToArtifactInTrash()),
                    "Failed to empty trash for repository '" + REPOSITORY_WITH_TRASH + "'!");
    }

    private void validateResponseBody(ValidatableMockMvcResponse response,
                                      String acceptHeader,
                                      String message)
    {
        if (acceptHeader.equals(MediaType.APPLICATION_JSON_VALUE))
        {
            response.body("message", equalTo(message));
        }
        else if (acceptHeader.equals(MediaType.TEXT_PLAIN_VALUE))
        {
            response.body(equalTo(message));
        }
        else
        {
            throw new IllegalArgumentException("Unsupported content type: " + acceptHeader);
        }
    }

}
