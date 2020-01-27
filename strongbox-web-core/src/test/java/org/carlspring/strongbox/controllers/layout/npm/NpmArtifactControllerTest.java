package org.carlspring.strongbox.controllers.layout.npm;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.NpmArtifactGenerator;
import org.carlspring.strongbox.booters.PropertiesBooter;
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

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.carlspring.strongbox.artifact.generator.ArtifactGenerator.DEFAULT_BYTES_SIZE;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class NpmArtifactControllerTest
        extends NpmRestAssuredBaseTest
{

    private static final String REPOSITORY_RELEASES = "npm-releases-test";

    @Inject
    PropertiesBooter propertiesBooter;

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
               .get(url, storageId, repositoryId, coordinates.buildResource())
               .then()
               .log().status()
               .log().headers()
               .statusCode(HttpStatus.OK.value())
               .assertThat()
               .header(HttpHeaders.CONTENT_LENGTH, equalTo(String.valueOf(Files.size(packagePath))));

        //Unpublish
        String unpublishURL = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{packageScope}/{packageName}"
                + "/-/{tarballName}/-rev/{rev}";
        String revision = "0-0000000000";
        String packageScope = "@carlspring";
        String packageName = "npm-test-release";
        String tarball = "npm-test-release-1.0.0.tgz";
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(unpublishURL, storageId, repositoryId, packageScope, packageName, tarball, revision)
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .assertThat()
               .statusCode(HttpStatus.OK.value());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void addUserTest(@NpmRepository(repositoryId = REPOSITORY_RELEASES)
                                    Repository repository)
    {
        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/" + NpmLayoutProvider.NPM_USER_PATH +
                     "{username}";
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

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @ParameterizedTest
    @ValueSource(strings = { "@angular/core:9.0.1-next.8",
                             "react:0.0.0-experimental-5faf377df",
                             "react:0.0.0-0c756fb-f7f79fd",
                             "react:0.0.0-5faf377df",
                             "prop-types:15.5.7-alpha.1",
                             "commander:4.0.0.0-1",
                             "aws-sdk:2.555.0",
                             "rxjs:5.6.0-forward-compat.5",
                             "@lifaon/observables:1.6.0" })
    public void packageNameAcceptanceTest(String packageNameWithVersion,
                                          @NpmRepository(repositoryId = REPOSITORY_RELEASES)
                                          Repository repository,
                                          TestInfo testInfo)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();


        Path generatorBasePath;

        //Splitting each string from paramterized test array to packageId and packageVersion
        final String[] packageDetails = packageNameWithVersion.split(":");
        final String packageId = packageDetails[0];
        final String packageVersion = packageDetails[1];

        //Manually generating each artifacts
        generatorBasePath = Paths.get(propertiesBooter.getVaultDirectory(),
                                      ".temp",
                                      testInfo.getTestClass().get().getSimpleName(),
                                      testInfo.getTestMethod().get().getName()).toAbsolutePath().normalize();

        NpmArtifactGenerator artifactGenerator = new NpmArtifactGenerator(generatorBasePath);
        Path artifact = artifactGenerator.generateArtifact(packageId, packageVersion, DEFAULT_BYTES_SIZE);

        NpmArtifactCoordinates coordinates = NpmArtifactCoordinates.of(packageId, packageVersion);

        Path publishJsonPath = artifact.resolveSibling("publish.json");
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
               .assertThat()
               .body("name", equalTo(packageId))
               .and()
               .assertThat()
               .body("versions." + "'" + packageVersion + "'" + ".version", equalTo(packageVersion))
               .statusCode(HttpStatus.OK.value());

        //Download
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, storageId, repositoryId, coordinates.buildResource())
               .then()
               .log().status()
               .log().headers()
               .statusCode(HttpStatus.OK.value())
               .assertThat()
               .header(HttpHeaders.CONTENT_LENGTH, equalTo(String.valueOf(Files.size(artifact))));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @ParameterizedTest
    @ValueSource(strings = { "@angular/core:next.8",
                             "react:@32.1.911",
                             "@lifaon/obser@323jj:hds:121",
                             "rxjs:assd5.6.0-hsds" })
    public void packageNameTestBadRequest(String packageNameWithVersion,
                                          @NpmRepository(repositoryId = REPOSITORY_RELEASES)
                                          Repository repository)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        //Splitting each string from parameterized test array to packageId and packageVersion
        final String[] packageDetails = packageNameWithVersion.split(":");
        final String packageId = packageDetails[0];
        final String packageVersion = packageDetails[1];
        String packageName = "";

        if (packageId.startsWith("@"))
        {
            packageName = packageId.split("/")[1];
        }
        else
        {
            packageName = packageId;
        }

        final String artifactFileName = String.format("%s-%s.%s", packageName, packageVersion, "tgz");
        final String artifactId = String.format("%s/-/%s", packageId, artifactFileName);

        //Download
        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{artifactId}";
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, storageId, repositoryId, artifactId)
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value());
    }

}
