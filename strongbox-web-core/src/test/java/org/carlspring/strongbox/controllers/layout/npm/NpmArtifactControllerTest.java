package org.carlspring.strongbox.controllers.layout.npm;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.artifact.generator.NpmArtifactGenerator;
import org.carlspring.strongbox.booters.PropertiesBooter;
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

import javax.inject.Inject;

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
    private PropertiesBooter propertiesBooter;

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
        Path artifact = artifactGenerator.generateArtifact(packageId, packageVersion);

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
               .body("versions." + "'" + packageVersion + "'" +".version", equalTo(packageVersion))
               .statusCode(HttpStatus.OK.value());

        //Download
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, storageId, repositoryId, coordinates.toResource())
               .prettyPeek()
               .then()
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
                                          Repository repository,
                                          TestInfo testInfo)
            throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        //Splitting each string from parameterized test array to packageId and packageVersion
        final String[] packageDetails = packageNameWithVersion.split(":");
        final String packageId = packageDetails[0];
        String packageName = "";
        final String packageVersion = packageDetails[1];

        if(packageId.startsWith("@"))
        {
            packageName = packageId.split("/")[1];
        }
        else
        {
            packageName = packageId;
        }

        final String artifactFileName = String.format("%s-%s.%s", packageName, packageVersion, "tgz");
        final String artifactId = String.format("%s/-/%s", packageId, artifactFileName );

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
