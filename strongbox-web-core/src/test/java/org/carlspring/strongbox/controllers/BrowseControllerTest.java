package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.domain.DirectoryListing;
import org.carlspring.strongbox.domain.FileContent;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Guido Grazioli
 * @author Pablo Tirado
 */
@IntegrationTest
public class BrowseControllerTest
        extends MavenRestAssuredBaseTest
{

    private static final String REPOSITORY = "browsing-test-repository";

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

        DirectoryListing returned = given().accept(MediaType.APPLICATION_JSON_VALUE)
                                           .when()
                                           .get(url)
                                           .prettyPeek()
                                           .as(DirectoryListing.class);

        assertNotNull(returned, "Failed to get storage list!");
        assertNotNull(returned.getDirectories(), "Failed to get storage list!");
        assertFalse(returned.getDirectories().isEmpty(), "Returned storage size does not match");

        List<FileContent> expectedSortedList = returned.getDirectories()
                                                       .stream()
                                                       .sorted(Comparator.comparing(FileContent::getName))
                                                       .collect(Collectors.toList());

        assertEquals(expectedSortedList, returned.getDirectories(), "Returned storages are not sorted!");

        String htmlResponse = given().accept(MediaType.TEXT_HTML_VALUE)
                                     .when()
                                     .get(url)
                                     .prettyPeek()
                                     .then()
                                     .statusCode(OK)
                                     .and()
                                     .extract()
                                     .asString();

        assertTrue(htmlResponse.contains(STORAGE0), "Returned HTML is incorrect");
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testGetRepositories(@MavenRepository(repositoryId = REPOSITORY)
                                    Repository repository)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String url = getContextBaseUrl() + "/{storageId}";

        DirectoryListing returned = given().accept(MediaType.APPLICATION_JSON_VALUE)
                                           .when()
                                           .get(url, storageId)
                                           .prettyPeek()
                                           .as(DirectoryListing.class);

        assertNotNull(returned, "Failed to get repository list!");
        assertNotNull(returned.getDirectories(), "Failed to get repository list!");
        assertFalse(returned.getDirectories().isEmpty(), "Returned repositories do not match");
        assertTrue(returned.getDirectories()
                           .stream()
                           .anyMatch(p -> p.getName().equals(repositoryId)), "Repository not found");

        List<FileContent> expectedSortedList = returned.getDirectories()
                                                       .stream()
                                                       .sorted(Comparator.comparing(FileContent::getName))
                                                       .collect(Collectors.toList());

        assertEquals(expectedSortedList, returned.getDirectories(), "Returned repositories are not sorted!");

        String htmlResponse = given().accept(MediaType.TEXT_HTML_VALUE)
                                     .when()
                                     .get(url, storageId)
                                     .prettyPeek()
                                     .then()
                                     .statusCode(OK)
                                     .and()
                                     .extract()
                                     .asString();

        assertTrue(htmlResponse.contains(repositoryId), "Returned HTML is incorrect");
    }

    @Test
    public void testGetRepositoriesWithStorageNotFound()
    {
        String url = getContextBaseUrl() + "/storagefoo";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());

        given().accept(MediaType.TEXT_HTML_VALUE)
               .when()
               .get(url)
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testRepositoryContents(@MavenRepository(repositoryId = REPOSITORY)
                                       Repository repository,
                                       @MavenTestArtifact(repositoryId = REPOSITORY,
                                                          id = "org.carlspring.strongbox.browsing:test-browsing",
                                                          versions = { "1.1",
                                                                       "3.2" })
                                       List<Path> artifactsPaths)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);

        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}/{artifactPath}";
        String artifactParent1PathStr = repositoryPath.relativize(artifactsPaths.get(0).getParent()).toString();

        DirectoryListing returned = given().accept(MediaType.APPLICATION_JSON_VALUE)
                                           .when()
                                           .get(url, storageId, repositoryId, artifactParent1PathStr)
                                           .prettyPeek()
                                           .as(DirectoryListing.class);

        assertTrue(returned.getFiles().size() == 6
                   && returned.getFiles().get(0).getName().equals("test-browsing-1.1.jar"), "Invalid files returned");

        String htmlResponse = given().accept(MediaType.TEXT_HTML_VALUE)
                                     .when()
                                     .get(url + "/", storageId, repositoryId, artifactParent1PathStr)
                                     .prettyPeek()
                                     .asString();

        String artifact1PathStr = FilenameUtils.separatorsToUnix(
                repositoryPath.relativize(artifactsPaths.get(0)).toString());
        String link = "/storages/" + storageId + "/" + repositoryId + "/" + artifact1PathStr;

        assertTrue(htmlResponse.contains(link), "Expected to have found [ " + link + " ] in the response html");
    }

    @Test
    public void testRepositoryContentsWithRepositoryNotFound()
    {
        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, STORAGE0, "repofoo")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());

        given().accept(MediaType.TEXT_HTML_VALUE)
               .when()
               .get(url, STORAGE0, "repofoo")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testRepositoryContentsWithPathNotFound()
    {
        String url = getContextBaseUrl() + "/{storageId}/{repositoryId}/{artifactPath}";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, STORAGE0, "releases", "foo/bar")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());

        given().accept(MediaType.TEXT_HTML_VALUE)
               .when()
               .get(url, STORAGE0, "releases", "foo/bar")
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
