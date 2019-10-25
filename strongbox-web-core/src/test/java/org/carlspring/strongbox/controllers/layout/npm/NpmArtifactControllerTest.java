package org.carlspring.strongbox.controllers.layout.npm;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
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
    public void addUserTest(@NpmRepository(repositoryId = REPOSITORY_RELEASES)
                                    Repository repository)
    {
        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/" + NpmLayoutProvider.NPM_USER_PATH + "{username}";
        String basicAuth = "Basic YWRtaW46cGFzc3dvcmQ=";

        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        NpmUser strongboxUser1 = new NpmUser("admin", "password");
        NpmUser strongboxUser2 = new NpmUser("deployer", "password");
        NpmUser nonStrongboxUser = new NpmUser("notARealUser", "notARealPassword");

        //can login with strongbox user
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(strongboxUser1)
               .when()
               .put(url, storageId, repositoryId, strongboxUser1.getName())
               .peek()
               .then()
               .statusCode(HttpStatus.CREATED.value());

        //can login with another strongbox user after login
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(strongboxUser2)
               .when()
               .put(url, storageId, repositoryId, strongboxUser2.getName())
               .peek()
               .then()
               .statusCode(HttpStatus.CREATED.value());

        //can't login when the url username differs from the body
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(strongboxUser1)
               .when()
               .put(url, storageId, repositoryId, nonStrongboxUser.getName())
               .peek()
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value());

        //can't login with non strongbox user when basic auth is strongbox user
        mockMvc.header(HttpHeaders.AUTHORIZATION, basicAuth)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(nonStrongboxUser)
               .when()
               .put(url, storageId, repositoryId, nonStrongboxUser.getName())
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value());

        //can login with strongbox user when basic auth is strongbox user
        mockMvc.header(HttpHeaders.AUTHORIZATION, basicAuth)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(strongboxUser1)
               .when()
               .put(url, storageId, repositoryId, strongboxUser1.getName())
               .then()
               .statusCode(HttpStatus.CREATED.value());

        //can't login with non-strongbox user
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(nonStrongboxUser)
               .when()
               .put(url, storageId, repositoryId, nonStrongboxUser.getName())
               .peek()
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

}
