package org.carlspring.strongbox.controllers.layout.pypi;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.rest.common.PypiRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.PypiTestArtifact;
import org.carlspring.strongbox.testing.repository.PypiTestRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import java.nio.file.Path;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
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

    @Override
    @BeforeEach
    public void init()
        throws Exception
    {
        super.init();
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testUploadPackageFlow(@PypiTestRepository(repositoryId = REPOSITORY_RELEASES, storageId = REPOSITORY_STORAGE) Repository repository,
                                      @PypiTestArtifact(repositoryId = REPOSITORY_RELEASES, storageId = REPOSITORY_STORAGE, id = "hello_world_pypi", versions = "1.3") Path packagePath)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        final RepositoryPath repositoryPath = (RepositoryPath) packagePath.normalize();

        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}";

        // Upload with Invalid action
        mockMvc.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
               .multiPart("filetype", "sdist")
               .multiPart(":action", "File_Upload")
               .multiPart("name", "hello-world-pipy")
               .multiPart("metadata_version", "1.0")
               .multiPart("content", repositoryPath.toFile())
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
               .multiPart("content", repositoryPath.toFile())
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
               .multiPart("content", repositoryPath.toFile())
               .when()
               .post(url, storageId, repositoryId)
               .then()
               .log()
               .all()
               .statusCode(HttpStatus.OK.value())
               .contentType(ContentType.TEXT)
               .body(Matchers.containsString("The artifact was deployed successfully."));
    }

    @Test
    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    public void testDownloadPackageRedirectionFlow(@PypiTestRepository(repositoryId = REPOSITORY_RELEASES, storageId = REPOSITORY_STORAGE) Repository repository)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final String packageName = "hello-world-pypi";

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
    public void testDownloadPackagePathListFlow(@PypiTestRepository(repositoryId = REPOSITORY_RELEASES, storageId = REPOSITORY_STORAGE) Repository repository,
                                                @PypiTestArtifact(repositoryId = REPOSITORY_RELEASES, storageId = REPOSITORY_STORAGE, id = "hello_world_pypi", versions = { "1.0",
                                                                                                                                                                            "2.0",
                                                                                                                                                                            "3.0",
                                                                                                                                                                            "3.1",
                                                                                                                                                                            "3.2",
                                                                                                                                                                            "3.4",
                                                                                                                                                                            "5.1" }) List<Path> packagePaths)
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        final String packageNameNotUploaded = "helloworldpypi";
        final String packageNameUploaded = "hello-world-pypi";

        final String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/simple/{packageName}/";

        // All packages list eligible for download :: package not uploaded
        mockMvc.when()
               .get(url, storageId, repositoryId, packageNameNotUploaded)
               .then()
               .log()
               .all()
               .statusCode(HttpStatus.OK.value())
               .contentType(ContentType.HTML)
               .body(Matchers.containsString("<title>Links for helloworldpypi</title>"),
                     Matchers.containsString("<h1>Links for helloworldpypi</h1>\n"),
                     Matchers.not(Matchers.containsString("<a href=")));

        // Upload different version of packages generated to be used by
        // next-case
        packagePaths.stream().forEach(path -> {

            final String uploadUrl = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}";

            mockMvc.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                   .multiPart("filetype", "sdist")
                   .multiPart(":action", "file_upload")
                   .multiPart("name", packageNameUploaded)
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
               .get(url, storageId, repositoryId, packageNameUploaded)
               .then()
               .log()
               .all()
               .statusCode(HttpStatus.OK.value())
               .contentType(ContentType.HTML)
               .body(Matchers.containsString("<title>Links for hello-world-pypi</title>"),
                     Matchers.containsString("<h1>Links for hello-world-pypi</h1>\n"),
                     Matchers.containsString("<a href="));

    }

}
