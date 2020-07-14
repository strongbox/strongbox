package org.carlspring.strongbox.controllers.layout.rpm;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.providers.io.LayoutFileSystem;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RootRepositoryPath;
import org.carlspring.strongbox.rest.common.RpmRestAssuredBaseTest;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.ExtractableResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test cases for {@link RpmArtifactController}.
 *
 * @author Martin Todorov
 */
@IntegrationTest
public class RpmArtifactControllerTest
        extends RpmRestAssuredBaseTest
{
    private static final Logger logger = LoggerFactory.getLogger(RpmArtifactControllerTest.class);

    private static final String TEST_RESOURCES = "target/test-resources";

    private static final String REPOSITORY_RELEASES1 = "releases-rpm";


    @Inject
    private ArtifactEntryService artifactEntryService;


    @BeforeAll
    static void setUpBeforeAll()
            throws IOException
    {
        Files.createDirectories(Paths.get(TEST_RESOURCES));
    }

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        MockitoAnnotations.initMocks(this);
    }

//    @AfterAll
//    static void down()
//    {
//        deleteTestResources();
//    }
//
//    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
//                  ArtifactManagementTestExecutionListener.class })
//    @Test
//    public void testHeadersFetch(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
//                                                  setup = MavenIndexedRepositorySetup.class)
//                                 Repository repository,
//                                 @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
//                                                    id = "org.carlspring.strongbox.browse:foo-bar",
//                                                    versions = "2.4")
//                                 Path artifactPath)
//    {
//        /* Hosted Repository */
//        RootRepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);
//        Path artifactJarPath = repositoryPath.relativize(artifactPath);
//        Path artifactPomPath = artifactJarPath.resolveSibling(
//                artifactJarPath.getFileName().toString().replace(".jar", ".pom"));
//
//        String url = getContextBaseUrl() + "/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES_1 + "/" +
//                     artifactPomPath.toString();
//
//        Headers headersFromGET = client.getHeadersFromGET(url);
//        Headers headersFromHEAD = client.getHeadersfromHEAD(url);
//        assertHeadersEquals(headersFromGET, headersFromHEAD);
//    }
//
//    private void assertHeadersEquals(Headers h1,
//                                     Headers h2)
//    {
//        assertThat(h1).isNotNull();
//        assertThat(h2).isNotNull();
//
//        for (Header header : h1)
//        {
//            if (h2.hasHeaderWithName(header.getName()))
//            {
//                assertThat(h2.getValue(header.getName())).isEqualTo(header.getValue());
//            }
//        }
//    }
//
//    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
//                  ArtifactManagementTestExecutionListener.class })
//    @Test
//    public void testDeleteArtifactFile(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
//                                                        setup = MavenIndexedRepositorySetup.class)
//                                       Repository repository,
//                                       @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
//                                                          id = "com.artifacts.to.delete.releases:delete-foo",
//                                                          versions = "1.2.1")
//                                       Path artifactPath)
//            throws Exception
//    {
//        final RepositoryPath artifactRepositoryPath = (RepositoryPath) artifactPath.normalize();
//        final String artifactRepositoryPathStr = RepositoryFiles.relativizePath(artifactRepositoryPath);
//
//        assertThat(Files.exists(artifactRepositoryPath))
//                .as("Failed to locate artifact file '" + artifactRepositoryPath + "'!")
//                .isTrue();
//
//        client.delete(repository.getStorage().getId(),
//                      repository.getId(),
//                      artifactRepositoryPathStr);
//
//        assertThat(Files.notExists(artifactRepositoryPath))
//                .as("Failed to delete artifact file '" + artifactRepositoryPath + "'!")
//                .isTrue();
//    }
//
//    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
//                  ArtifactManagementTestExecutionListener.class })
//    @Test
//    public void testDirectoryListing(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
//                                                      setup = MavenIndexedRepositorySetup.class)
//                                     Repository repository,
//                                     @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
//                                                        id = "org.carlspring.strongbox.browse:foo-bar",
//                                                        versions = "1.0")
//                                     Path artifactPath)
//    {
//        assertThat(Files.exists(artifactPath)).as("Failed to locate artifact file '" + artifactPath + "'!").isTrue();
//
//        String basePath = "/api/browse/" + repository.getStorage().getId() + "/" + repository.getId();
//
//        ExtractableResponse repositoryRoot = client.getResourceWithResponse(basePath, "");
//        ExtractableResponse trashDirectoryListing = client.getResourceWithResponse(basePath, ".trash");
//        ExtractableResponse directoryListing = client.getResourceWithResponse(basePath,
//                                                                              "org/carlspring/strongbox/browse/");
//        ExtractableResponse fileListing = client.getResourceWithResponse(basePath,
//                                                                         "org/carlspring/strongbox/browse/foo-bar/1.0/");
//        ExtractableResponse invalidPath = client.getResourceWithResponse(basePath,
//                                                                         "org/carlspring/strongbox/browse/1.0/");
//
//        String repositoryRootContent = repositoryRoot.asString();
//        String directoryListingContent = directoryListing.asString();
//        String fileListingContent = fileListing.asString();
//
//        assertThat(repositoryRootContent.contains(".trash"))
//                .as(".trash directory should not be visible in directory listing!")
//                .isFalse();
//        assertThat(trashDirectoryListing.response().getStatusCode())
//                .as(".trash directory should not be browsable!")
//                .isEqualTo(HttpStatus.NOT_FOUND.value());
//
//        logger.debug(directoryListingContent);
//
//        assertThat(directoryListingContent.contains("org/carlspring/strongbox/browse")).isTrue();
//        assertThat(fileListingContent.contains("foo-bar-1.0.jar")).isTrue();
//        assertThat(fileListingContent.contains("foo-bar-1.0.pom")).isTrue();
//
//        assertThat(invalidPath.response().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
//
//        assertThat(repositoryRootContent.contains(LayoutFileSystem.INDEX))
//                .as(".index directory should not be visible in directory listing!")
//                .isFalse();
//    }
//
//    @Test
//    public void shouldDownloadProxiedSnapshotArtifactFromRemote()
//            throws Exception
//    {
//        ArtifactSnapshotVersion commonsHttpSnapshot = getCommonsHttpArtifactSnapshotVersionFromCarlspringRemote();
//
//        if (commonsHttpSnapshot == null)
//        {
//            logger.debug("commonsHttpSnapshot was not found");
//            return;
//        }
//
//        String url = getContextBaseUrl() + "/storages/{storageId}/{repositoryId}/{artifactPath}";
//        String artifactPath = String.format("org/carlspring/commons/commons-http/%s/commons-http-%s.jar",
//                                            commonsHttpSnapshot.version, commonsHttpSnapshot.timestampedVersion);
//
//        mockMvc.header(HttpHeaders.USER_AGENT, "Maven/*")
//               .contentType(MediaType.TEXT_PLAIN_VALUE)
//               .when()
//               .get(url, "storage-common-proxies", "carlspring", artifactPath)
//               .then()
//               .statusCode(HttpStatus.OK.value());
//
//        ArtifactEntry artifactEntry = artifactEntryService.findOneArtifact("storage-common-proxies",
//                                                                           "carlspring",
//                                                                           artifactPath);
//        assertThat(artifactEntry).isNotNull();
//        assertThat(artifactEntry.getArtifactCoordinates()).isNotNull();
//
//        assertThat(artifactEntry).isInstanceOf(RemoteArtifactEntry.class);
//        assertThat(((RemoteArtifactEntry) artifactEntry).getIsCached()).isTrue();
//    }

}
