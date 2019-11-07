package org.carlspring.strongbox.utils;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.utils.ArtifactControllerHelper.MULTIPART_BOUNDARY;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
class ArtifactControllerHelperTest
{

    private static final String REPOSITORY_RELEASES_1 = "acht-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "acht-releases-2";

    private static final String REPOSITORY_RELEASES_3 = "acht-releases-3";

    private static final String REPOSITORY_RELEASES_4 = "acht-releases-4";

    private static final String REPOSITORY_RELEASES_5 = "acht-releases-5";

    private static final String REPOSITORY_RELEASES_6 = "acht-releases-6";

    private static final String REPOSITORY_RELEASES_7 = "acht-releases-7";

    private static final String REPOSITORY_RELEASES_8 = "acht-releases-8";

    @Inject
    private ArtifactResolutionService artifactResolutionService;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;

    private static Stream<Arguments> isRangedRequestProvider()
    {
        return Stream.of(
                Arguments.of("5-", true),
                Arguments.of("1-99", true),
                Arguments.of("0-100,200-300", true),
                Arguments.of("0-100, 200-300", false),
                Arguments.of("0/*", false),
                Arguments.of("0-", false),
                Arguments.of("0", false),
                Arguments.of("", false)
        );
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    void handlePartialDownloadWithSingleRange(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1)
                                              Repository repository,
                                              @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                                 id = "org.carlspring.strongbox:partial-single",
                                                                 versions = "1.0")
                                              Path artifactPath)
            throws IOException
    {
        // Given
        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        try(InputStream is = artifactResolutionService.getInputStream(artifactRepositoryPath))
        {
            HttpHeaders httpHeaders = getHttpHeaders("100-199");
            HttpServletResponse response = new MockHttpServletResponse();

            // When
            ArtifactControllerHelper.handlePartialDownload(is, httpHeaders, response);


            // Then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.PARTIAL_CONTENT.value());
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    void shouldNotHandlePartialDownloadWithSingleRangeWhenRequestedRangeNotSatisfiable(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_2)
            Repository repository,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_2,
                               id = "org.carlspring.strongbox:partial-single-not-satisfiable",
                               versions = "1.0")
            Path artifactPath)
            throws IOException
    {
        // Given
        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        try(InputStream is = artifactResolutionService.getInputStream(artifactRepositoryPath))
        {
            long expectedLength = Files.size(artifactRepositoryPath);
            String byteRanges = String.format("%s-%s", expectedLength, expectedLength + 1);
            HttpHeaders httpHeaders = getHttpHeaders(byteRanges);
            HttpServletResponse response = new MockHttpServletResponse();

            // When
            ArtifactControllerHelper.handlePartialDownload(is, httpHeaders, response);

            // Then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
            assertThat(response.getHeader(HttpHeaders.CONTENT_RANGE)).isEqualTo("bytes */" + expectedLength);
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    void shouldNotHandlePartialDownloadWithSingleRangeWhenOffsetGreaterThanLimit(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_3)
            Repository repository,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_3,
                               id = "org.carlspring.strongbox:partial-multiple-not-satisfiable-validation",
                               versions = "1.0")
            Path artifactPath)
            throws IOException
    {
        // Given
        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        try(InputStream is = artifactResolutionService.getInputStream(artifactRepositoryPath))
        {
            String byteRanges = "49-0";
            HttpHeaders httpHeaders = getHttpHeaders(byteRanges);
            HttpServletResponse response = new MockHttpServletResponse();

            // When
            ArtifactControllerHelper.handlePartialDownload(is, httpHeaders, response);

            // Then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
            assertThat(response.getHeader(HttpHeaders.CONTENT_RANGE)).startsWith("bytes */");
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    void handlePartialDownloadWithMultipleRanges(@MavenRepository(repositoryId = REPOSITORY_RELEASES_4)
                                                 Repository repository,
                                                 @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_4,
                                                                    id = "org.carlspring.strongbox:partial-multiple",
                                                                    versions = "1.0")
                                                 Path artifactPath)
            throws IOException
    {
        // Given
        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        try(InputStream is = artifactResolutionService.getInputStream(artifactRepositoryPath))
        {
            HttpHeaders httpHeaders = getHttpHeaders("0-500,510-519");
            HttpServletResponse response = new MockHttpServletResponse();
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(Files.size(artifactRepositoryPath)));

            // When
            ArtifactControllerHelper.handlePartialDownload(is, httpHeaders, response);

            // Then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.PARTIAL_CONTENT.value());
            assertThat(response.getContentType()).isEqualTo("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    void shouldNotHandlePartialDownloadWithMultipleRangesWhenRequestedRangeNotSatisfiable(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_5)
            Repository repository,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_5,
                               id = "org.carlspring.strongbox:partial-multiple-not-satisfiable",
                               versions = "1.0")
            Path artifactPath)
            throws IOException
    {
        // Given
        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        try(InputStream is = artifactResolutionService.getInputStream(artifactRepositoryPath))
        {
            long expectedLength = Files.size(artifactRepositoryPath);
            String byteRanges = String.format("0-49,%s-%s", expectedLength, expectedLength + 1);
            HttpHeaders httpHeaders = getHttpHeaders(byteRanges);
            HttpServletResponse response = new MockHttpServletResponse();

            // When
            ArtifactControllerHelper.handlePartialDownload(is, httpHeaders, response);

            // Then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
            assertThat(response.getHeader(HttpHeaders.CONTENT_RANGE)).isEqualTo("bytes */" + expectedLength);
        }
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    void shouldNotHandlePartialDownloadWithMultipleRangesWhenOffsetGreaterThanLimit(
            @MavenRepository(repositoryId = REPOSITORY_RELEASES_6)
            Repository repository,
            @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_6,
                               id = "org.carlspring.strongbox:partial-multiple-not-satisfiable-validation",
                               versions = "1.0")
            Path artifactPath)
            throws IOException
    {
        // Given
        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
        try(InputStream is = artifactResolutionService.getInputStream(artifactRepositoryPath))
        {
            String byteRanges = "49-0,60-55";
            HttpHeaders httpHeaders = getHttpHeaders(byteRanges);
            HttpServletResponse response = new MockHttpServletResponse();

            // When
            ArtifactControllerHelper.handlePartialDownload(is, httpHeaders, response);

            // Then
            assertThat(response.getStatus()).isEqualTo(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
            assertThat(response.getHeader(HttpHeaders.CONTENT_RANGE)).startsWith("bytes */");
        }
    }

    @ParameterizedTest
    @MethodSource(value = "isRangedRequestProvider")
    void isRangedRequest(String byteRanges,
                         boolean expectedResult)
    {
        // Given
        HttpHeaders httpHeaders = getHttpHeaders(byteRanges);

        // When
        boolean actualResult = ArtifactControllerHelper.isRangedRequest(httpHeaders);

        // Then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @ParameterizedTest
    @ValueSource(strings = { "test/test.jar",
                             "" })
    void provideArtifactHeadersNotFound(String pathStr,
                                        @MavenRepository(repositoryId = REPOSITORY_RELEASES_7)
                                        Repository repository)
            throws IOException
    {
        // Given
        HttpServletResponse response = new MockHttpServletResponse();
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository).resolve(pathStr);

        // When
        ArtifactControllerHelper.provideArtifactHeaders(response, repositoryPath);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    void provideArtifactHeaders(@MavenRepository(repositoryId = REPOSITORY_RELEASES_8)
                                Repository repository,
                                @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_8,
                                                   id = "org.carlspring.strongbox:provide-artifact-headers",
                                                   versions = "1.0")
                                Path artifactPath)
            throws IOException
    {
        // Given
        HttpServletResponse response = new MockHttpServletResponse();
        RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();

        // When
        ArtifactControllerHelper.provideArtifactHeaders(response, artifactRepositoryPath);

        // Then
        assertThat(response.getHeader(HttpHeaders.CONTENT_LENGTH)).isNotEmpty();
        assertThat(response.getHeader(HttpHeaders.LAST_MODIFIED)).isNotEmpty();
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        assertThat(response.getHeader(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
    }


    private HttpHeaders getHttpHeaders(String byteRanges)
    {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.RANGE, "bytes=" + byteRanges);

        return httpHeaders;
    }
}
