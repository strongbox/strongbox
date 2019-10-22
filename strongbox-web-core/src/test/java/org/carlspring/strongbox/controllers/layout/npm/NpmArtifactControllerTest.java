package org.carlspring.strongbox.controllers.layout.npm;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.rest.common.NpmRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NpmTestArtifact;
import org.carlspring.strongbox.testing.repository.NpmRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class NpmArtifactControllerTest
        extends NpmRestAssuredBaseTest
{
    private static final String REPOSITORY_RELEASES = "npm-releases-test";

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testViewPackage(@NpmRepository(repositoryId = REPOSITORY_RELEASES)
                                Repository repository,
                                @NpmTestArtifact(repositoryId = REPOSITORY_RELEASES,
                                                 id = "npm-test-view",
                                                 versions = "1.0.0",
                                                 scope = "@carlspring")
                                Path packagePath)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        RepositoryPath normPackagePath = (RepositoryPath) packagePath.normalize();
        NpmArtifactCoordinates coordinates = (NpmArtifactCoordinates) RepositoryFiles.readCoordinates(normPackagePath);

        // View OK
        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{artifactId}/{version}";
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, storageId, repositoryId, coordinates.getId(), coordinates.getVersion())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        // View 404
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, storageId, repositoryId, coordinates.getId(), "1.0.1")
               .peek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void testPackageCommonFlow(@NpmRepository(repositoryId = REPOSITORY_RELEASES)
                                      Repository repository,
                                      @NpmTestArtifact(id = "npm-test-release",
                                                       versions = "1.0.0",
                                                       scope = "@carlspring")
                                      Path packagePath)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final String packageId = "@carlspring/npm-test-release";
        final String packageVersion = "1.0.0";

        NpmArtifactCoordinates coordinates = NpmArtifactCoordinates.of(packageId, packageVersion);

        Path publishJsonPath = packagePath.resolveSibling("publish.json");
        byte[] publishJsonContent = Files.readAllBytes(publishJsonPath);

        //Publish
        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{artifactId}";
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(publishJsonContent)
               .when()
               .put(url, storageId, repositoryId, coordinates.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        //View
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, storageId, repositoryId, coordinates.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        //Download
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, storageId, repositoryId, coordinates.toResource())
               .then()
               .statusCode(HttpStatus.OK.value())
               .assertThat()
               .header(HttpHeaders.CONTENT_LENGTH, equalTo(String.valueOf(Files.size(packagePath))));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void packageNameAcceptanceTest(@NpmRepository(repositoryId = REPOSITORY_RELEASES)
                                          Repository repository,
                                          @NpmTestArtifact(id = "npm-test-release",
                                                           versions = "1.0.0",
                                                           scope = "@carlspring")
                                          Path packagePath1,
                                          @NpmTestArtifact(id = "core",
                                                           versions = "9.0.1-next.8",
                                                           scope = "@angular")
                                          Path packagePath2,
                                          @NpmTestArtifact(id = "some-package",
                                                           versions = "4.0.4-alpha.3",
                                                           scope = "@value")
                                          Path packagePath3)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        //Artifact#1
        final String packageId1 = "@carlspring/npm-test-release";
        final String packageVersion1 = "1.0.0";

        NpmArtifactCoordinates coordinates1 = NpmArtifactCoordinates.of(packageId1, packageVersion1);

        Path publishJsonPath1 = packagePath1.resolveSibling("publish.json");
        byte[] publishJsonContent1 = Files.readAllBytes(publishJsonPath1);

        //Publish
        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{artifactId}";
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(publishJsonContent1)
                .when()
                .put(url, storageId, repositoryId, coordinates1.getId())
                .peek()
                .then()
                .statusCode(HttpStatus.OK.value());

        //View
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(url, storageId, repositoryId, coordinates1.getId())
                .peek()
                .then()
                .statusCode(HttpStatus.OK.value());

        //Download
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(url, storageId, repositoryId, coordinates1.toResource())
                .then()
                .statusCode(HttpStatus.OK.value())
                .assertThat()
                .header(HttpHeaders.CONTENT_LENGTH, equalTo(String.valueOf(Files.size(packagePath1))));

        //Artifact#2
        final String packageId2 = "@angular/core";
        final String packageVersion2 = "9.0.1-next.8";

        NpmArtifactCoordinates coordinates2 = NpmArtifactCoordinates.of(packageId2, packageVersion2);

        Path publishJsonPath2 = packagePath2.resolveSibling("publish.json");
        byte[] publishJsonContent2 = Files.readAllBytes(publishJsonPath2);

        //Publish
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(publishJsonContent2)
                .when()
                .put(url, storageId, repositoryId, coordinates2.getId())
                .peek()
                .then()
                .statusCode(HttpStatus.OK.value());

        //View
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(url, storageId, repositoryId, coordinates2.getId())
                .peek()
                .then()
                .statusCode(HttpStatus.OK.value());

        //Download
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(url, storageId, repositoryId, coordinates2.toResource())
                .then()
                .statusCode(HttpStatus.OK.value())
                .assertThat()
                .header(HttpHeaders.CONTENT_LENGTH, equalTo(String.valueOf(Files.size(packagePath2))));

        //Artifact#3
        final String packageId3 = "@value/some-package";
        final String packageVersion3 = "4.0.4-alpha.3";

        NpmArtifactCoordinates coordinates3 = NpmArtifactCoordinates.of(packageId3, packageVersion3);

        Path publishJsonPath3 = packagePath3.resolveSibling("publish.json");
        byte[] publishJsonContent3 = Files.readAllBytes(publishJsonPath3);

        //Publish
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(publishJsonContent3)
                .when()
                .put(url, storageId, repositoryId, coordinates3.getId())
                .peek()
                .then()
                .statusCode(HttpStatus.OK.value());

        //View
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(url, storageId, repositoryId, coordinates3.getId())
                .peek()
                .then()
                .statusCode(HttpStatus.OK.value());

        //Download
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(url, storageId, repositoryId, coordinates3.toResource())
                .then()
                .statusCode(HttpStatus.OK.value())
                .assertThat()
                .header(HttpHeaders.CONTENT_LENGTH, equalTo(String.valueOf(Files.size(packagePath3))));

    }

}