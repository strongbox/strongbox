package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.MavenRepositoryFactory;
import org.carlspring.strongbox.storage.repository.RepositoryDto;
import org.carlspring.strongbox.yaml.configuration.repository.MavenRepositoryConfigurationDto;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
public class TrashControllerUndeleteTest
        extends MavenRestAssuredBaseTest
{

    private static final String REPOSITORY_WITH_TRASH = "tcut-releases-with-trash";

    private static final String REPOSITORY_RELEASES = "tcut-releases";

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
        MavenRepositoryConfigurationDto mavenRepositoryConfiguration = new MavenRepositoryConfigurationDto();
        mavenRepositoryConfiguration.setIndexingEnabled(true);

        RepositoryDto repositoryWithTrash = mavenRepositoryFactory.createRepository(REPOSITORY_WITH_TRASH);
        repositoryWithTrash.setAllowsForceDeletion(false);
        repositoryWithTrash.setTrashEnabled(true);
        repositoryWithTrash.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(STORAGE0, repositoryWithTrash);

        // Notes:
        // - Used by testForceDeleteArtifactAllowed()
        // - Forced deletions are allowed
        RepositoryDto repositoryReleases = mavenRepositoryFactory.createRepository(REPOSITORY_RELEASES);
        repositoryReleases.setAllowsForceDeletion(false);
        repositoryReleases.setRepositoryConfiguration(mavenRepositoryConfiguration);

        createRepository(STORAGE0, repositoryReleases);

        setUp();
    }

    private void setUp()
            throws Exception
    {
        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_TRASH).getAbsolutePath(),
                         "org.carlspring.strongbox.undelete:test-artifact-undelete",
                         new String[]{ "1.0",
                                       "1.1" });

        generateArtifact(getRepositoryBasedir(STORAGE0, REPOSITORY_RELEASES).getAbsolutePath(),
                         "org.carlspring.strongbox.undelete:test-artifact-undelete",
                         new String[]{ "2.0" });

        // Delete the artifact (this one should get placed under the .trash)
        client.delete(STORAGE0,
                      REPOSITORY_WITH_TRASH,
                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar");
        client.delete(STORAGE0,
                      REPOSITORY_WITH_TRASH,
                      "org/carlspring/strongbox/undelete/test-artifact-undelete/1.1/test-artifact-undelete-1.1.jar");

        // Delete the artifact (this one shouldn't get placed under the .trash)
        client.delete(STORAGE0,
                      REPOSITORY_RELEASES,
                      "org/carlspring/strongbox/undelete/test-artifact-undelete/2.0/test-artifact-undelete-2.0.jar");
    }

    @Override
    @AfterEach
    public void shutdown()
    {
        try
        {
            removeRepositories();
        }
        catch (IOException | JAXBException e)
        {
            throw new UndeclaredThrowableException(e);
        }

        super.shutdown();
    }

    private void removeRepositories()
            throws IOException, JAXBException
    {
        removeRepositories(getRepositoriesToClean());
    }

    public static Set<RepositoryDto> getRepositoriesToClean()
    {
        Set<RepositoryDto> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_WITH_TRASH, Maven2LayoutProvider.ALIAS));
        repositories.add(createRepositoryMock(STORAGE0, REPOSITORY_RELEASES, Maven2LayoutProvider.ALIAS));

        return repositories;
    }

    @Test
    public void testUndeleteArtifactFromTrashForRepository()
            throws Exception
    {
        String url = getContextBaseUrl() + "/api/trash/" + STORAGE0 + "/" + REPOSITORY_WITH_TRASH +
                     "/org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/test-artifact-undelete-1.0.jar";

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .when()
               .post(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(
                       "The trash for '" + STORAGE0 + ":" + REPOSITORY_WITH_TRASH +
                       "' was restored successfully."));

        final Path artifactFileRestoredFromTrash = Paths.get(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_TRASH).getAbsolutePath() + "/" +
                                                             "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                             "test-artifact-undelete-1.0.jar");

        final Path artifactFileInTrash = Paths.get(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_TRASH).getAbsolutePath() + "/.trash/" +
                                                   "org/carlspring/strongbox/undelete/test-artifact-undelete/1.0/" +
                                                   "test-artifact-undelete-1.0.jar");

        assertFalse(Files.exists(artifactFileInTrash),
                    "Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!");
        assertTrue(Files.exists(artifactFileRestoredFromTrash),
                   "Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!");
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testUndeleteArtifactsForAllRepositories(String acceptHeader)
            throws Exception
    {
        final Path artifactFileInTrash = Paths.get(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_TRASH).getAbsolutePath() + "/.trash/" +
                                                   "org/carlspring/strongbox/undelete/test-artifact-undelete/1.1/" +
                                                   "test-artifact-undelete-1.1.jar");

        assertTrue(Files.exists(artifactFileInTrash.getParent()),
                   "Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!");

        String url = getContextBaseUrl() + "/api/trash";

        ValidatableMockMvcResponse response = given().accept(acceptHeader)
                                                     .when()
                                                     .post(url)
                                                     .peek()
                                                     .then()
                                                     .statusCode(HttpStatus.OK.value());

        String message = "The trash for all repositories was successfully restored.";
        validateResponseBody(response, acceptHeader, message);

        final Path artifactFileRestoredFromTrash = Paths.get(getRepositoryBasedir(STORAGE0, REPOSITORY_WITH_TRASH).getAbsolutePath() + "/" +
                                                             "org/carlspring/strongbox/undelete/test-artifact-undelete/1.1/" +
                                                             "test-artifact-undelete-1.1.jar");

        assertFalse(Files.exists(artifactFileInTrash),
                    "Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH + "'!");
        assertTrue(Files.exists(artifactFileRestoredFromTrash),
                   "Failed to undelete trash for repository '" + REPOSITORY_WITH_TRASH +
                           "' (" + artifactFileRestoredFromTrash.toAbsolutePath().toString() + " does not exist)!");
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
