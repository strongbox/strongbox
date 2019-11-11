package org.carlspring.strongbox.controllers.layout.raw;

import org.carlspring.strongbox.artifact.generator.RawArtifactGenerator;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.rest.common.RawRestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.TestArtifact;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.utils.ArtifactControllerHelper.MULTIPART_BOUNDARY;

/**
 * @author Martin Todorov
 * @author Pablo Tirado
 */
@IntegrationTest
public class RawArtifactControllerTest
        extends RawRestAssuredBaseTest
{

    private static final String REPOSITORY_RELEASES_1 = "ract-raw-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "ract-raw-releases-2";

    private static final String REPOSITORY_RELEASES_3 = "ract-raw-releases-3";

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testDeploy(@TestRepository(layout = RawLayoutProvider.ALIAS,
                                           repositoryId = REPOSITORY_RELEASES_1)
                           Repository repository)
            throws IOException
    {
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String path = "org/foo/bar/blah.txt";
        byte[] content = "This is a test file\n".getBytes();

        // Push
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" + path;

        mockMvc.header(HttpHeaders.USER_AGENT, "Raw/*")
               .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
               .body(content)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        assertPathExists(url);

        InputStream is = client.getResource(url);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        int len;
        final int size = 64;
        byte[] bytes = new byte[size];
        while ((len = is.read(bytes, 0, size)) != -1)
        {
            baos.write(bytes, 0, len);
        }

        baos.flush();

        assertThat(new String(baos.toByteArray())).as("Deployed content mismatch!").isEqualTo(new String(content));

        logger.debug("Read '{}',", new String(baos.toByteArray()));
    }

    @ExtendWith({RepositoryManagementTestExecutionListener.class,
                 ArtifactManagementTestExecutionListener.class })
    @Test
    public void testResolveViaHostedRepository(@TestRepository(layout = RawLayoutProvider.ALIAS,
                                                               repositoryId = REPOSITORY_RELEASES_1)
                                               Repository repository,
                                               @TestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                             resource = "org/foo/bar/blah.zip",
                                                             generator = RawArtifactGenerator.class)
                                               Path artifactPath)
    {
        final String pathStr = "org/foo/bar/blah.zip";

        RepositoryPath artifactRepositoryPath = repositoryPathResolver.resolve(repository, pathStr);
        assertThat(Files.exists(artifactRepositoryPath.toAbsolutePath())).as("Artifact does not exist!").isTrue();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldHandlePartialDownloadWithSingleRange(@TestRepository(layout = RawLayoutProvider.ALIAS,
                                                                           repositoryId = REPOSITORY_RELEASES_2)
                                                           Repository repository,
                                                           @TestArtifact(repositoryId = REPOSITORY_RELEASES_2,
                                                                         resource = "org/carlspring/strongbox/raw/test/partial-download-single.zip",
                                                                         generator = RawArtifactGenerator.class)
                                                           Path artifactPath)
    {
        final String byteRanges = "100-199";
        final String pathStr = "org/carlspring/strongbox/raw/test/partial-download-single.zip";

        MockMvcResponse response = getMockMvcResponseForPartialDownload(byteRanges,
                                                                        repository,
                                                                        pathStr);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PARTIAL_CONTENT.value());
        assertThat(response.getHeader(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldHandlePartialDownloadWithMultipleRanges(@TestRepository(layout = RawLayoutProvider.ALIAS,
                                                                             repositoryId = REPOSITORY_RELEASES_3)
                                                              Repository repository,
                                                              @TestArtifact(repositoryId = REPOSITORY_RELEASES_3,
                                                                            resource = "org/carlspring/strongbox/raw/test/partial-download-multiple.zip",
                                                                            generator = RawArtifactGenerator.class)
                                                              Path artifactPath)
    {
        final String byteRanges = "0-29,200-249,300-309";
        final String pathStr = "org/carlspring/strongbox/raw/test/partial-download-multiple.zip";

        MockMvcResponse response = getMockMvcResponseForPartialDownload(byteRanges,
                                                                        repository,
                                                                        pathStr);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PARTIAL_CONTENT.value());
        assertThat(response.getHeader(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.getContentType()).isEqualTo("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
    }

    private MockMvcResponse getMockMvcResponseForPartialDownload(String byteRanges,
                                                                 Repository repository,
                                                                 String pathStr)
    {
        // Given
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{path}";

        // When
        return mockMvc.header(HttpHeaders.RANGE, "bytes=" + byteRanges)
                      .contentType(MediaType.TEXT_PLAIN_VALUE)
                      .when()
                      .get(url, storageId, repositoryId, pathStr)
                      .thenReturn();
    }
}

