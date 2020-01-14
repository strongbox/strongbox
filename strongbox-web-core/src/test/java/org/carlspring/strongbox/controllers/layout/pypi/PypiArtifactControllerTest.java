package org.carlspring.strongbox.controllers.layout.pypi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.PypiArtifactCoordinates;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.rest.common.PypiRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.PypiTestArtifact;
import org.carlspring.strongbox.testing.repository.PypiTestRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.http.ContentType;

/**
 * 
 * @author ankit.tomar
 *
 */
@IntegrationTest
public class PypiArtifactControllerTest extends PypiRestAssuredBaseTest
{
    
    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testUploadPackage(@PypiTestRepository(repositoryId = REPOSITORY_RELEASES, storageId = REPOSITORY_STORAGE) Repository repository,
                                  @PypiTestArtifact(id = "hello_world_pypi", versions = "1.3") Path packagePath)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}";

        PypiArtifactCoordinates coordinates = new PypiArtifactCoordinates("hello_world_pypi",
                                                                          "1.3",
                                                                          null,
                                                                          "py3",
                                                                          "none",
                                                                          "any",
                                                                          "whl");
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository, coordinates);
        assertThat(Files.exists(repositoryPath)).isFalse();
        
        // Upload with Invalid action
        mockMvc.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
               .multiPart("filetype", "sdist")
               .multiPart(":action", "File_Upload")
               .multiPart("name", "hello-world-pipy")
               .multiPart("metadata_version", "1.0")
               .multiPart("content", packagePath.toFile())
               .when()
               .post(url, storageId, repositoryId)
               .then()
               .log()
               .all()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .contentType(ContentType.TEXT);

        // Upload with Invalid filetype
        mockMvc.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
               .multiPart("filetype", "wheel")
               .multiPart(":action", "file_upload")
               .multiPart("name", "hello-world-pipy")
               .multiPart("metadata_version", "1.0")
               .multiPart("content", packagePath.toFile())
               .when()
               .post(url, storageId, repositoryId)
               .then()
               .log()
               .all()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .contentType(ContentType.TEXT);

        // Valid pypi package upload
        mockMvc.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
               .multiPart("filetype", "sdist")
               .multiPart(":action", "file_upload")
               .multiPart("name", "hello-world-pipy")
               .multiPart("metadata_version", "1.0")
               .multiPart("content", packagePath.toFile())
               .when()
               .post(url, storageId, repositoryId)
               .then()
               .log()
               .all()
               .statusCode(HttpStatus.OK.value())
               .contentType(ContentType.TEXT)
               .body(Matchers.containsString("The artifact was deployed successfully."));
        
        repositoryPath = repositoryPathResolver.resolve(repository, coordinates);
        assertThat(Files.exists(repositoryPath)).isTrue();
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testDownloadPackageRedirect(@PypiTestRepository(repositoryId = REPOSITORY_RELEASES, storageId = REPOSITORY_STORAGE) Repository repository,
                                            @PypiTestArtifact(repositoryId = REPOSITORY_RELEASES, storageId = REPOSITORY_STORAGE, id = "hello_world_pypi", versions = "1.3") Path packagePath)
    {
        final PypiArtifactCoordinates coordinates = PypiArtifactCoordinates.parse(packagePath.getFileName().toString());

        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final String packageName = coordinates.getId();

        final String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{packageName}/";

        final String expectedRedirectionUrl = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId
                + "/simple/" + packageName + "/";

        // Download Package redirection
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, storageId, repositoryId, packageName)
               .then()
               .log()
               .all()
               .statusCode(HttpStatus.SEE_OTHER.value())
               .contentType(ContentType.TEXT)
               .header(HttpHeaders.LOCATION, Matchers.is(expectedRedirectionUrl));

    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testBrowsePackage(@PypiTestRepository(repositoryId = REPOSITORY_RELEASES, storageId = REPOSITORY_STORAGE) Repository repository,
                                  @PypiTestArtifact(storageId = REPOSITORY_STORAGE, id = "hello_world_pypi", versions = { "1.0",
                                                                                                                          "2.0",
                                                                                                                          "3.0",
                                                                                                                          "3.1",
                                                                                                                          "3.2",
                                                                                                                          "3.4",
                                                                                                                          "5.1" }) List<Path> packagePaths)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        final String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/simple/{packageName}/";

        // All packages list eligible for download :: package not uploaded
        mockMvc.when()
               .get(url, storageId, repositoryId, "hello_world_pypi")
               .then()
               .log()
               .all()
               .statusCode(HttpStatus.OK.value())
               .contentType(ContentType.HTML)
               .body(Matchers.containsString("<title>Not Found</title>"),
                     Matchers.containsString("<h1>Not Found</h1>\n"),
                     Matchers.not(Matchers.containsString("<a href=")));

        // Upload different version of packages generated to be used by
        // next-case
        packagePaths.stream().forEach(path -> {

            final String uploadUrl = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}";

            mockMvc.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                   .multiPart("filetype", "sdist")
                   .multiPart(":action", "file_upload")
                   .multiPart("name", "hello_world_pypi")
                   .multiPart("metadata_version", "1.0")
                   .multiPart("content", path.toFile())
                   .when()
                   .post(uploadUrl, storageId, repositoryId)
                   .then()
                   .log()
                   .all()
                   .statusCode(HttpStatus.OK.value())
                   .contentType(ContentType.TEXT)
                   .body(Matchers.containsString("The artifact was deployed successfully."));
        });

        // All packages list eligible for download :: package upload
        mockMvc.when()
               .get(url, storageId, repositoryId, "hello_world_pypi")
               .then()
               .log()
               .all()
               .statusCode(HttpStatus.OK.value())
               .contentType(ContentType.HTML)
               .body(Matchers.containsString("<title>Links for hello_world_pypi</title>"),
                     Matchers.containsString("<h1>Links for hello_world_pypi</h1>\n"),
                     Matchers.containsString("<a href="));

    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testDownloadPackage(@PypiTestRepository(repositoryId = REPOSITORY_RELEASES, storageId = REPOSITORY_STORAGE) Repository repository,
                                    @PypiTestArtifact(repositoryId = REPOSITORY_RELEASES, storageId = REPOSITORY_STORAGE, id = "hello_world_pypi", versions = { "1.0",
                                                                                                                                                                "2.0",
                                                                                                                                                                "3.0",
                                                                                                                                                                "3.1",
                                                                                                                                                                "3.2",
                                                                                                                                                                "3.4",
                                                                                                                                                                "5.1" }) List<Path> packagePaths)
        throws IOException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        final String invalidPackageName = "helloworldpypi";
        final String packageNameNotUploaded = "helloworldpypi-1.0-py3-none-any.whl";

        final String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/packages/{packageName}";

        // Download request for invalid artifact/package name.
        mockMvc.when()
               .get(url, storageId, repositoryId, invalidPackageName)
               .then()
               .log()
               .all()
               .statusCode(HttpStatus.BAD_REQUEST.value());

        // Download request for package not uploaded.
        mockMvc.when()
               .get(url, storageId, repositoryId, packageNameNotUploaded)
               .then()
               .log()
               .all()
               .statusCode(HttpStatus.NOT_FOUND.value());

        // Download different versions of package
        packagePaths.stream().forEach(path -> {

            // Download request for valid and uploaded package.
            long size;
            try
            {
                size = Files.size(path.normalize());
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            mockMvc.when()
                   .get(url, storageId, repositoryId, path.getFileName().toString())
                   .then()
                   .log()
                   .status()
                   .log()
                   .headers()
                   .statusCode(HttpStatus.OK.value())
                   .contentType(MediaType.APPLICATION_OCTET_STREAM_VALUE)
                   .header(HttpHeaders.CONTENT_LENGTH, Matchers.equalTo(String.valueOf(size)));
        });
    }
}
