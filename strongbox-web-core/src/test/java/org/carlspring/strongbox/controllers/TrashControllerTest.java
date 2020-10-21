package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.repository.MavenRepositoryFeatures;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryAttributes;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author Martin Todorov
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@IntegrationTest
public class TrashControllerTest
        extends MavenRestAssuredBaseTest
{
    private static final String REPOSITORY_WITH_TRASH_1 = "tct-releases-with-trash-1";

    private static final String REPOSITORY_WITH_TRASH_2 = "tct-releases-with-trash-2";

    private static final String REPOSITORY_WITH_TRASH_3 = "tct-releases-with-trash-3";

    private static final String REPOSITORY_WITH_FORCE_DELETE_1 = "tct-releases-with-force-delete-1";

    private static final String REPOSITORY_WITH_FORCE_DELETE_2 = "tct-releases-with-force-delete-2";

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        setContextBaseUrl("/api/trash");
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testForceDeleteArtifactNotAllowed(@MavenRepository(repositoryId = REPOSITORY_WITH_TRASH_1)
                                                  @RepositoryAttributes(trashEnabled = true)
                                                  Repository repository,
                                                  @MavenTestArtifact(repositoryId = REPOSITORY_WITH_TRASH_1,
                                                                     id = "org.carlspring.strongbox:test-artifact-to-trash",
                                                                     versions = "1.0")
                                                  Path artifactPath)
            throws IOException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        final RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        final String artifactRepositoryPathStr = RepositoryFiles.relativizePath(artifactRepositoryPath);

        // Delete the artifact (this one should get placed under the .trash)
        client.delete(storageId, repositoryId, artifactRepositoryPathStr, false);

        final Path artifactFile = RepositoryFiles.trash(artifactRepositoryPath);

        logger.debug("Artifact file: {}", artifactFile.toAbsolutePath());

        assertThat(Files.exists(artifactFile))
                .as("Should have moved the artifact to the trash during a force delete operation, " +
                   "when allowsForceDeletion is not enabled!")
                .isTrue();

        final RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);
        final Path repositoryIndexDir = repositoryPath.resolve(MavenRepositoryFeatures.INDEX);

        assertThat(Files.exists(repositoryIndexDir)).as("Should not have deleted .index directory!").isTrue();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testForceDeleteArtifactAllowed(@MavenRepository(repositoryId = REPOSITORY_WITH_FORCE_DELETE_1)
                                               Repository repository,
                                               @MavenTestArtifact(repositoryId = REPOSITORY_WITH_FORCE_DELETE_1,
                                                                  id = "org.carlspring.strongbox:test-artifact-to-trash",
                                                                  versions = "1.1")
                                               Path artifactPath)
            throws IOException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        final RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        final String artifactRepositoryPathStr = RepositoryFiles.relativizePath(artifactRepositoryPath);

        // Delete the artifact (this one shouldn't get placed under the .trash)
        client.delete(storageId, repositoryId, artifactRepositoryPathStr, true);

        final Path artifactFileInTrash = RepositoryFiles.trash(artifactRepositoryPath);

        final RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);
        final Path repositoryDir = repositoryPath.resolve(repositoryId).resolve(".trash");

        assertThat(Files.exists(artifactFileInTrash))
                .as("Failed to delete artifact during a force delete operation!")
                .isFalse();
        assertThat(Files.exists(repositoryDir.resolve(artifactRepositoryPathStr)))
                .as("Failed to delete artifact during a force delete operation!")
                .isFalse();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testDeleteArtifactAndEmptyTrashForRepository(String acceptHeader,
                                                      @MavenRepository(repositoryId = REPOSITORY_WITH_TRASH_2)
                                                      @RepositoryAttributes(trashEnabled = true)
                                                      Repository repository,
                                                      @MavenTestArtifact(repositoryId = REPOSITORY_WITH_TRASH_2,
                                                                         id = "org.carlspring.strongbox:test-artifact-to-trash",
                                                                         versions = "1.0")
                                                      Path artifactPath)
            throws IOException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}";

        ValidatableMockMvcResponse response = mockMvc.accept(acceptHeader)
                                                     .when()
                                                     .delete(url, storageId, repositoryId)
                                                     .peek()
                                                     .then()
                                                     .statusCode(HttpStatus.OK.value());

        String message = String.format("The trash for '%s:%s' was removed successfully.", storageId, repositoryId);
        validateResponseBody(response, acceptHeader, message);

        final RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        final Path artifactFileInTrash = RepositoryFiles.trash(artifactRepositoryPath);
        assertThat(Files.exists(artifactFileInTrash))
                .as("Failed to empty trash for repository '" + repositoryId + "'!")
                .isFalse();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testDeleteArtifactAndEmptyTrashForAllRepositories(String acceptHeader,
                                                           @MavenRepository(repositoryId = REPOSITORY_WITH_TRASH_3)
                                                           @RepositoryAttributes(trashEnabled = true)
                                                           Repository repository1,
                                                           @MavenTestArtifact(repositoryId = REPOSITORY_WITH_TRASH_3,
                                                                              id = "org.carlspring.strongbox:test-artifact-to-trash",
                                                                              versions = "1.0")
                                                           Path artifactPath1,
                                                           @MavenRepository(repositoryId = REPOSITORY_WITH_FORCE_DELETE_2)
                                                           Repository repository2,
                                                           @MavenTestArtifact(repositoryId = REPOSITORY_WITH_FORCE_DELETE_2,
                                                                              id = "org.carlspring.strongbox:test-artifact-to-trash",
                                                                              versions = "1.1")
                                                           Path artifactPath2)
            throws IOException
    {
        String url = getContextBaseUrl();

        ValidatableMockMvcResponse response = mockMvc.accept(acceptHeader)
                                                     .when()
                                                     .delete(url)
                                                     .peek()
                                                     .then()
                                                     .statusCode(HttpStatus.OK.value());

        String message = "The trash for all repositories was successfully removed.";
        validateResponseBody(response, acceptHeader, message);

        final RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath1.normalize();
        final Path artifactFileInTrash = RepositoryFiles.trash(artifactRepositoryPath);
        assertThat(Files.exists(artifactFileInTrash))
                .as("Failed to empty trash for repository '" + repository1.getId() + "'!")
                .isFalse();
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
