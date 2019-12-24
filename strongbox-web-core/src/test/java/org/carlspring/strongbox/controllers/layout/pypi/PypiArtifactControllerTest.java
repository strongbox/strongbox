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
                                      @PypiTestArtifact(repositoryId = REPOSITORY_RELEASES, storageId = REPOSITORY_STORAGE, id = "hello_world_pipy", versions = "1.3") Path packagePath)
        throws Exception
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
        throws Exception
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();
        final String packageName = "hello-world-pypi";

        final String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{packageName}/";

        final String expectedRedirectionUrl = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId
                + "/simple/" + packageName + "/";

        // Upload with Invalid action
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

}
