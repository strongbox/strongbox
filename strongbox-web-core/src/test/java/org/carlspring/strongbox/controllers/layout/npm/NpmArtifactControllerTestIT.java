package org.carlspring.strongbox.controllers.layout.npm;

import org.carlspring.strongbox.artifact.coordinates.NpmArtifactCoordinates;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.rest.common.NpmRestAssuredBaseTest;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.NpmTestArtifact;
import org.carlspring.strongbox.testing.repository.NpmRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;

import javax.inject.Inject;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class NpmArtifactControllerTestIT
        extends NpmRestAssuredBaseTest
{

    private static final String REPOSITORY_PROXY_RESOLVE = "nactit-npm-proxy-resolve";

    private static final String REPOSITORY_GROUP_PROXY_RESOLVE = "nactit-npm-group-proxy-resolve";

    private static final String REPOSITORY_GROUP_RESOLVE = "nactit-npm-group-resolve";

    private static final String REPOSITORY_PROXY_SEARCH = "nactit-npm-proxy-search";

    private static final String REPOSITORY_PROXY_VIEW = "nactit-npm-proxy-view";


    private static final String REPOSITORY_RELEASES_1 = "nactit-npm-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "nactit-npm-releases-2";

    private static final String REMOTE_URL = "https://registry.npmjs.org/";

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Override
    @BeforeEach
    public void init()
        throws Exception
    {
        super.init();
    }

    /**
     * Note: This test requires an Internet connection.
     *
     * @throws Exception
     */
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testResolveArtifactViaProxy(@Remote(url = REMOTE_URL)
                                            @NpmRepository(repositoryId = REPOSITORY_PROXY_RESOLVE)
                                            Repository proxyRepository)
            throws Exception
    {
        // https://registry.npmjs.org/compression/-/compression-1.7.2.tgz
        String artifactPath =
                "/storages/" + proxyRepository.getStorage().getId() + "/" + proxyRepository.getId() + "/" +
                "compression/-/compression-1.7.2.tgz";

        resolveArtifact(artifactPath);
    }

    /**
     * Note: This test requires an Internet connection.
     *
     * @throws Exception
     */
    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testResolveArtifactViaGroupWithProxy(@Remote(url = REMOTE_URL)
                                                     @NpmRepository(repositoryId = REPOSITORY_GROUP_PROXY_RESOLVE)
                                                     Repository proxyRepository,
                                                     @Group(repositories = REPOSITORY_GROUP_PROXY_RESOLVE)
                                                     @NpmRepository(repositoryId = REPOSITORY_GROUP_RESOLVE)
                                                     Repository groupRepository)
            throws Exception
    {
        // https://registry.npmjs.org/compression/-/compression-1.7.2.tgz
        String artifactPath =
                "/storages/" + groupRepository.getStorage().getId() + "/" + groupRepository.getId() + "/" +
                "compression/-/compression-1.7.2.tgz";

        resolveArtifact(artifactPath);
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testViewArtifactViaProxy(@Remote(url = REMOTE_URL)
                                         @NpmRepository(repositoryId = REPOSITORY_PROXY_VIEW)
                                         Repository proxyRepository)
    {
        final String storageId = proxyRepository.getStorage().getId();
        final String repositoryId = proxyRepository.getId();

        NpmArtifactCoordinates coordinates = NpmArtifactCoordinates.of("react", "16.5.0");

        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{artifactId}";
        mockMvc.when()
               .get(url, storageId, repositoryId, coordinates.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .and()
               .body("name", equalTo("react"))
               .body("versions.size()", greaterThan(0));

        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact(storageId,
                                                                           repositoryId,
                                                                           coordinates.toPath());
        assertThat(artifactEntry).isNotNull();
        assertThat(artifactEntry).isInstanceOf(RemoteArtifactEntry.class);
        assertThat(((RemoteArtifactEntry)artifactEntry).getIsCached()).isFalse();
    }

    @ExtendWith(RepositoryManagementTestExecutionListener.class)
    @Test
    public void testSearchArtifactViaProxy(@Remote(url = REMOTE_URL)
                                           @NpmRepository(repositoryId = REPOSITORY_PROXY_SEARCH)
                                           Repository proxyRepository)
    {
        final String storageId = proxyRepository.getStorage().getId();
        final String repositoryId = proxyRepository.getId();

        String url = getContextBaseUrl() +
                     "/storages/{storageId}/{repositoryId}/-/v1/search?text=reston&size=10";
        mockMvc.when()
               .get(url, storageId, repositoryId)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .and()
               .body("objects.package.name", hasItem("Reston"));
        
        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact(storageId,
                                                                           repositoryId,
                                                                           "Reston/Reston/0.2.0/Reston-0.2.0.tgz");
        assertThat(artifactEntry).isNotNull();
        assertThat(artifactEntry).isInstanceOf(RemoteArtifactEntry.class);
        assertThat(((RemoteArtifactEntry)artifactEntry).getIsCached()).isFalse();
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldHandlePartialDownloadWithSingleRange(@NpmRepository(repositoryId = REPOSITORY_RELEASES_1)
                                                           Repository repository,
                                                           @NpmTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                                            id = "npm-partial-download-single",
                                                                            versions = "1.0")
                                                           Path artifactPath)
    {
        final String byteRanges = "100-199";
        final String packageId = "npm-partial-download-single";
        final String packageVersion = "1.0";
        final String packageExtension = "tgz";

        MockMvcResponse response = getMockMvcResponseForPartialDownload(byteRanges,
                                                                        repository,
                                                                        packageId,
                                                                        packageVersion,
                                                                        packageExtension);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PARTIAL_CONTENT.value());
        assertThat(response.getHeader(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldHandlePartialDownloadWithMultipleRanges(@NpmRepository(repositoryId = REPOSITORY_RELEASES_2)
                                                              Repository repository,
                                                              @NpmTestArtifact(repositoryId = REPOSITORY_RELEASES_2,
                                                                               id = "npm-partial-download-multiple",
                                                                               versions = "1.0")
                                                              Path artifactPath)
    {
        final String byteRanges = "0-29,200-249,300-309";
        final String packageId = "npm-partial-download-multiple";
        final String packageVersion = "1.0";
        final String packageExtension = "tgz";

        MockMvcResponse response = getMockMvcResponseForPartialDownload(byteRanges,
                                                                        repository,
                                                                        packageId,
                                                                        packageVersion,
                                                                        packageExtension);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PARTIAL_CONTENT.value());
        assertThat(response.getHeader(HttpHeaders.ACCEPT_RANGES)).isEqualTo("bytes");
        assertThat(response.getContentType()).isEqualTo("multipart/byteranges; boundary=" + MULTIPART_BOUNDARY);
    }

    private MockMvcResponse getMockMvcResponseForPartialDownload(String byteRanges,
                                                                 Repository repository,
                                                                 String packageName,
                                                                 String packageVersion,
                                                                 String packageExtension)
    {
        // Given
        final String storageId = repository.getStorage().getId();
        final String repositoryId = repository.getId();

        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{packageName}/-/{packageName}-{packageVersion}.{packageExtension}";

        // When
        return mockMvc.header(HttpHeaders.RANGE, "bytes=" + byteRanges)
                      .contentType(MediaType.TEXT_PLAIN_VALUE)
                      .when()
                      .get(url, storageId, repositoryId, packageName, packageName, packageVersion, packageExtension)
                      .thenReturn();
    }
}
