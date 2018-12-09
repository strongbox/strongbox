package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.domain.DirectoryListing;
import org.carlspring.strongbox.domain.FileContent;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Guido Grazioli
 * @author Pablo Tirado
 */
@IntegrationTest
public class BrowseControllerTest
        extends MavenRestAssuredBaseTest
{

    private static final String REPOSITORY_1 = "browsing-test-repository-1";
    private static final String REPOSITORY_2 = "browsing-test-repository-2";

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        setContextBaseUrl(BrowseController.ROOT_CONTEXT);
    }

    @Test
    public void testGetStorages()
    {
        String url = getContextBaseUrl() + "/";

        DirectoryListing returned = mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
                                           .when()
                                           .get(url)
                                           .prettyPeek()
                                           .as(DirectoryListing.class);

        assertThat(returned).as("Failed to get storage list!").isNotNull();
        assertThat(returned.getDirectories()).as("Failed to get storage list!").isNotNull();
        assertThat(returned.getDirectories()).as("Returned storage size does not match").isNotEmpty();

        List<FileContent> expectedSortedList = returned.getDirectories()
                                                       .stream()
                                                       .sorted(Comparator.comparing(FileContent::getName))
                                                       .collect(Collectors.toList());

        assertThat(returned.getDirectories()).as("Returned storages are not sorted!").isEqualTo(expectedSortedList);

        String htmlResponse = mockMvc.accept(MediaType.TEXT_HTML_VALUE)
                                     .when()
                                     .get(url)
                                     .prettyPeek()
                                     .then()
                                     .statusCode(OK)
                                     .and()
                                     .extract()
                                     .asString();

        assertThat(htmlResponse.contains(STORAGE0)).as("Returned HTML is incorrect").isTrue();
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testGetRepositories(@MavenRepository(repositoryId = REPOSITORY_1)
                                    Repository repository)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String url = getContextBaseUrl() + "/{storageId}";

        DirectoryListing returned = mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
                                           .when()
                                           .get(url, storageId)
                                           .prettyPeek()
                                           .as(DirectoryListing.class);

        assertThat(returned).as("Failed to get repository list!").isNotNull();
        assertThat(returned.getDirectories()).as("Failed to get repository list!").isNotNull();
        assertThat(returned.getDirectories()).as("Returned repositories do not match").isNotEmpty();
        assertThat(returned.getDirectories()
                           .stream()
                           .anyMatch(p -> p.getName().equals(repositoryId)))
                .as("Repository not found")
                .isTrue();

        List<FileContent> expectedSortedList = returned.getDirectories()
                                                       .stream()
                                                       .sorted(Comparator.comparing(FileContent::getName))
                                                       .collect(Collectors.toList());

        assertThat(returned.getDirectories()).as("Returned repositories are not sorted!").isEqualTo(expectedSortedList);

        String htmlResponse = mockMvc.accept(MediaType.TEXT_HTML_VALUE)
                                     .when()
                                     .get(url, storageId)
                                     .prettyPeek()
                                     .then()
                                     .statusCode(OK)
                                     .and()
                                     .extract()
                                     .asString();

        assertThat(htmlResponse.contains(repositoryId)).as("Returned HTML is incorrect").isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_HTML_VALUE })
    public void testGetRepositoriesWithStorageNotFound(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/storagefoo";

        mockMvc.accept(acceptHeader)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());

    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRepositoryContents(@MavenRepository(repositoryId = REPOSITORY_2)
                                       Repository repository,
                                       @MavenTestArtifact(repositoryId = REPOSITORY_2,
                                                          id = "org.carlspring.strongbox.browsing:test-browsing",
                                                          versions = { "1.1",
                                                                       "3.2" })
                                       List<Path> artifactsPaths)
            throws IOException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}/{artifactPath}";

        RepositoryPath artifact1Path = (RepositoryPath) artifactsPaths.get(0).normalize();
        String artifact1PathStr = RepositoryFiles.relativizePath(artifact1Path);
        String unixBasedRelativePath = FilenameUtils.separatorsToUnix(artifact1PathStr);

        RepositoryPath artifact1ParentPath = artifact1Path.getParent();
        String artifact1ParentPathStr = RepositoryFiles.relativizePath(artifact1ParentPath);

        DirectoryListing returned = mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
                                           .when()
                                           .get(url, storageId, repositoryId, artifact1ParentPathStr)
                                           .prettyPeek()
                                           .as(DirectoryListing.class);

        assertThat(returned.getFiles().size() == 6
                   && returned.getFiles().get(0).getName().equals("test-browsing-1.1.jar"))
                .as("Invalid files returned").isTrue();

        String htmlResponse = mockMvc.accept(MediaType.TEXT_HTML_VALUE)
                                     .when()
                                     .get(url + "/", storageId, repositoryId, artifact1ParentPathStr)
                                     .prettyPeek()
                                     .asString();

        String link = "/storages/" + storageId + "/" + repositoryId + "/" + unixBasedRelativePath;

        assertThat(htmlResponse.contains(link)).as("Expected to have found [ " + link + " ] in the response html").isTrue();
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_HTML_VALUE })
    public void testRepositoryContentsWithRepositoryNotFound(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}";

        mockMvc.accept(acceptHeader)
               .when()
               .get(url, STORAGE0, "repofoo")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_HTML_VALUE })
    public void testRepositoryContentsWithPathNotFound(String acceptHeader)
    {
        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}/{artifactPath}";

        mockMvc.accept(acceptHeader)
               .when()
               .get(url, STORAGE0, "releases", "foo/bar")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
