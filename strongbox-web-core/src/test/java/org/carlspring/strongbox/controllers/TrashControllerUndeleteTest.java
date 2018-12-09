package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryAttributes;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import org.junit.jupiter.api.BeforeEach;
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
public class TrashControllerUndeleteTest
        extends MavenRestAssuredBaseTest
{
    private static final String REPOSITORY_WITH_TRASH_1 = "tcut-releases-with-trash-1";

    private static final String REPOSITORY_WITH_TRASH_2 = "tcut-releases-with-trash-2";

    private static final String REPOSITORY_RELEASES = "tcut-releases";

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
    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testUndeleteArtifactFromTrashForRepository(String acceptHeader,
                                                    @MavenRepository(repositoryId = REPOSITORY_WITH_TRASH_1,
                                                                     setup = MavenIndexedRepositorySetup.class)
                                                    @RepositoryAttributes(trashEnabled = true)
                                                    Repository repository,
                                                    @MavenTestArtifact(repositoryId = REPOSITORY_WITH_TRASH_1,
                                                                       id = "org.carlspring.strongbox.undelete:test-artifact-undelete",
                                                                       versions =  { "1.0",
                                                                                     "1.1" })
                                                    List<Path> artifactsPaths)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        final RepositoryPath artifact10Path = (RepositoryPath) artifactsPaths.get(0).normalize();
        final String artifact10PathStr = RepositoryFiles.relativizePath(artifact10Path);

        final RepositoryPath artifact11Path = (RepositoryPath) artifactsPaths.get(1).normalize();
        final String artifact11PathStr = RepositoryFiles.relativizePath(artifact11Path);

        // Delete the artifact (this one should get placed under the .trash)
        client.delete(storageId,
                      repositoryId,
                      artifact10PathStr);

        client.delete(storageId,
                      repositoryId,
                      artifact11PathStr);

        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}/{artifactPath}";

        ValidatableMockMvcResponse response = mockMvc.accept(acceptHeader)
                                                     .when()
                                                     .post(url, storageId, repositoryId, artifact10PathStr)
                                                     .peek()
                                                     .then()
                                                     .statusCode(HttpStatus.OK.value());

        String message = String.format("The trash for '%s:%s' was restored successfully.", storageId, repositoryId);
        validateResponseBody(response, acceptHeader, message);

        final Path artifactFileInTrash = RepositoryFiles.trash(artifact10Path);

        assertThat(Files.exists(artifactFileInTrash))
                .as("Failed to undelete trash for repository '" + repositoryId + "'!")
                .isFalse();
        assertThat(Files.exists(artifact10Path))
                .as("Failed to undelete trash for repository '" + repositoryId + "'!")
                .isTrue();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testUndeleteArtifactsForAllRepositories(String acceptHeader,
                                                 @MavenRepository(repositoryId = REPOSITORY_WITH_TRASH_2,
                                                                  setup = MavenIndexedRepositorySetup.class)
                                                 @RepositoryAttributes(trashEnabled = true)
                                                 Repository repository1,
                                                 @MavenTestArtifact(repositoryId = REPOSITORY_WITH_TRASH_2,
                                                                    id = "org.carlspring.strongbox.undelete:test-artifact-undelete",
                                                                    versions =  { "1.0",
                                                                                  "1.1" })
                                                 List<Path> artifact1Paths,
                                                 @MavenRepository(repositoryId = REPOSITORY_RELEASES,
                                                                  setup = MavenIndexedRepositorySetup.class)
                                                 Repository repository2,
                                                 @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                                    id = "org.carlspring.strongbox.undelete:test-artifact-undelete",
                                                                    versions =  "2.0" )
                                                 Path artifact2Path)
            throws Exception
    {
        // Repository with trash enabled, and 2 artifacts.
        final String storageId = repository1.getStorage().getId();
        final String repository1Id = repository1.getId();

        final RepositoryPath artifact10Path = (RepositoryPath) artifact1Paths.get(0).normalize();
        final String artifact10PathStr = RepositoryFiles.relativizePath(artifact10Path);

        final RepositoryPath artifact11Path = (RepositoryPath) artifact1Paths.get(1).normalize();
        final String artifact11PathStr = RepositoryFiles.relativizePath(artifact11Path);

        // Delete the 2 artifacts (this ones should get placed under the .trash)
        client.delete(storageId,
                      repository1Id,
                      artifact10PathStr);

        client.delete(storageId,
                      repository1Id,
                      artifact11PathStr);

        // Repository without trash enabled, and 1 artifact.
        final String repository2Id = repository2.getId();
        final RepositoryPath artifact2RepositoryPath = (RepositoryPath) artifact2Path.normalize();
        final String artifact2PathStr = RepositoryFiles.relativizePath(artifact2RepositoryPath);

        // Delete the artifact (this one shouldn't get placed under the .trash)
        client.delete(storageId,
                      repository2Id,
                      artifact2PathStr);

        final Path artifactFileInTrash = RepositoryFiles.trash(artifact10Path);

        assertThat(Files.exists(artifactFileInTrash.getParent()))
                .as("Failed to undelete trash for repository '" + repository1Id + "'!")
                .isTrue();

        String url = getContextBaseUrl();

        ValidatableMockMvcResponse response = mockMvc.accept(acceptHeader)
                                                     .when()
                                                     .post(url)
                                                     .peek()
                                                     .then()
                                                     .statusCode(HttpStatus.OK.value());

        String message = "The trash for all repositories was successfully restored.";
        validateResponseBody(response, acceptHeader, message);

        assertThat(Files.exists(artifactFileInTrash))
                .as("Failed to undelete trash for repository '" + repository1Id + "'!")
                .isFalse();
        assertThat(Files.exists(artifact10Path))
                .as("Failed to undelete trash for repository '" + repository1Id +
                   "' (" + artifact10Path.toAbsolutePath() + " does not exist)!")
                .isTrue();
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
