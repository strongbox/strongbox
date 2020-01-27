package org.carlspring.strongbox.controllers.layout.maven;

import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.net.MediaType.APPLICATION_X_GZIP_VALUE;
import static org.hamcrest.CoreMatchers.equalTo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.inject.Inject;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.carlspring.strongbox.storage.indexing.IndexTypeEnum;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexCreator.RepositoryIndexCreatorQualifier;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexDirectoryPathResolver;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexDirectoryPathResolver.RepositoryIndexDirectoryPathResolverQualifier;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.artifact.ArtifactManagementTestExecutionListener;
import org.carlspring.strongbox.testing.artifact.MavenTestArtifact;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Group;
import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;
import org.carlspring.strongbox.util.MessageDigestUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import io.restassured.http.Header;

/**
 * @author Kate Novik
 * @author Martin Todorov
 */
@IntegrationTest
public class MavenIndexControllerTest
        extends MavenRestAssuredBaseTest
{

    private static final String REPOSITORY_RELEASES_1 = "aict-releases-1";

    private static final String REPOSITORY_RELEASES_2 = "aict-releases-2";

    private static final String REPOSITORY_RELEASES_3 = "aict-releases-3";

    private static final String REPOSITORY_RELEASES_4 = "aict-releases-4";

    private static final String REPOSITORY_RELEASES_5 = "aict-releases-5";

    private static final String REPOSITORY_RELEASES_6 = "aict-releases-6";

    private static final String REPOSITORY_RELEASES_6_1 = "aict-releases-6_1";

    private static final String REPOSITORY_RELEASES_6_GROUP = "aict-releases-6-group";

    private static final String REPOSITORY_PROXY_1 = "aict-proxy-1";

    private static final String PROXY_1_REPOSITORY_URL =
            "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES_1 + "/";

    private static final String REPOSITORY_PROXY_3 = "aict-proxy-3";

    private static final String PROXY_3_REPOSITORY_URL =
            "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES_3 + "/";

    private static final String REPOSITORY_PROXY_4 = "aict-proxy-4";

    private static final String PROXY_4_REPOSITORY_URL =
            "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES_4 + "/";

    private static final String REPOSITORY_PROXY_5 = "aict-proxy-5";

    private static final String PROXY_5_REPOSITORY_URL =
            "http://localhost:48080/storages/" + STORAGE0 + "/" + REPOSITORY_RELEASES_5 + "/";

    private static final String PROPERTIES_INJECTOR_GROUP_ID = "org.carlspring";

    private static final String PROPERTIES_INJECTOR_ARTIFACT_ID = "properties-injector";

    private static final String SLF4J_GROUP_ID = "org.slf4j";

    private static final String SLF4J_ARTIFACT_ID = "slf4j-log4j12";

    @Inject
    @RepositoryIndexDirectoryPathResolverQualifier(IndexTypeEnum.LOCAL)
    private RepositoryIndexDirectoryPathResolver repositoryLocalIndexDirectoryPathResolver;

    @Inject
    @RepositoryIndexDirectoryPathResolverQualifier(IndexTypeEnum.REMOTE)
    private RepositoryIndexDirectoryPathResolver repositoryRemoteIndexDirectoryPathResolver;

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.HOSTED)
    private RepositoryIndexCreator hostedRepositoryIndexCreator;

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.PROXY)
    private RepositoryIndexCreator proxyRepositoryIndexCreator;

    @Inject
    @RepositoryIndexCreatorQualifier(RepositoryTypeEnum.GROUP)
    private RepositoryIndexCreator groupRepositoryIndexCreator;

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
    public void hostedRepositoryIndexShouldBeRegeneratedOnDemand(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                                                  setup = MavenIndexedRepositorySetup.class)
                                                                 Repository repository,
                                                                 @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                                                    id = "org.carlspring.strongbox.indexes:strongbox-test",
                                                                                    versions = { "1.0",
                                                                                                 "1.1" },
                                                                                    classifiers = { "javadoc",
                                                                                                    "sources" })
                                                                 List<Path> artifactPaths)
            throws IOException, NoSuchAlgorithmException
    {
        hostedRepositoryIndexCreator.apply(repository);

        RepositoryPath indexPath = repositoryLocalIndexDirectoryPathResolver.resolve(repository)
                                                                            .resolve("nexus-maven-repository-index.gz");
        String beforeChecksum = MessageDigestUtils.calculateChecksum(indexPath, "SHA-1");

        String url = getContextBaseUrl() + "/api/maven/index/{storageId}/{repositoryId}";

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .post(url, repository.getStorage().getId(), repository.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        indexPath = repositoryLocalIndexDirectoryPathResolver.resolve(repository).resolve(
                "nexus-maven-repository-index.gz");

        String afterChecksum = MessageDigestUtils.calculateChecksum(indexPath, "SHA-1");

        assertThat(beforeChecksum).isNotEqualTo(afterChecksum);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldNotAllowToRebuildTheIndex(@MavenRepository(repositoryId = REPOSITORY_RELEASES_2)
                                                Repository repository,
                                                @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_2,
                                                                   id = "org.carlspring.strongbox.indexes:strongbox-test",
                                                                   versions = { "1.0",
                                                                                "1.1" },
                                                                   classifiers = { "javadoc",
                                                                                   "sources" })
                                                List<Path> artifactPaths)
    {
        RepositoryPath indexPath = repositoryLocalIndexDirectoryPathResolver.resolve(repository)
                                                                            .resolve("nexus-maven-repository-index.gz");
        assertThat(indexPath).doesNotExist();

        String url = getContextBaseUrl() + "/api/maven/index/{storageId}/{repositoryId}";

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .post(url, repository.getStorage().getId(), repository.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(equalTo("Indexing is disabled on this repository."));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void proxyRepositoryIndexShouldBeReFetchedOnDemand(@MavenRepository(repositoryId = REPOSITORY_RELEASES_1,
                                                                               setup = MavenIndexedRepositorySetup.class)
                                                              Repository repository,
                                                              @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_1,
                                                                                 id = "org.carlspring.strongbox.indexes:strongbox-test",
                                                                                 versions = { "1.0",
                                                                                              "1.1" },
                                                                                 classifiers = { "javadoc",
                                                                                                 "sources" })
                                                              List<Path> artifactPaths,
                                                              @MavenRepository(repositoryId = REPOSITORY_PROXY_1,
                                                                               setup = MavenIndexedRepositorySetup.class)
                                                              @Remote(url = PROXY_1_REPOSITORY_URL)
                                                              Repository proxyRepository)
            throws IOException
    {
        hostedRepositoryIndexCreator.apply(repository);

        RepositoryPath indexPath = repositoryRemoteIndexDirectoryPathResolver.resolve(proxyRepository)
                                                                             .resolve("nexus-maven-repository-index.gz");

        assertThat(indexPath).doesNotExist();

        String url = getContextBaseUrl() + "/api/maven/index/{storageId}/{repositoryId}";

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .post(url, proxyRepository.getStorage().getId(), proxyRepository.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        indexPath = repositoryRemoteIndexDirectoryPathResolver.resolve(proxyRepository)
                                                              .resolve("nexus-maven-repository-index.gz");

        assertThat(indexPath).matches(Files::exists);
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldNotAllowToReFetchTheIndex(@MavenRepository(repositoryId = REPOSITORY_RELEASES_3,
                                                                 setup = MavenIndexedRepositorySetup.class)
                                                Repository repository,
                                                @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_3,
                                                                   id = "org.carlspring.strongbox.indexes" + ":" + "strongbox-test",
                                                                   versions = { "1.0",
                                                                                "1.1" },
                                                                   classifiers = { "javadoc",
                                                                                   "sources" })
                                                List<Path> artifactPaths,
                                                @MavenRepository(repositoryId = REPOSITORY_PROXY_3)
                                                @Remote(url = PROXY_3_REPOSITORY_URL)
                                                Repository proxyRepository)
    {
        RepositoryPath indexPath = repositoryRemoteIndexDirectoryPathResolver.resolve(repository)
                                                                             .resolve("nexus-maven-repository-index.gz");

        assertThat(indexPath).doesNotExist();

        String url = getContextBaseUrl() + "/api/maven/index/{storageId}/{repositoryId}";

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .post(url, proxyRepository.getStorage().getId(), proxyRepository.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(equalTo("Indexing is disabled on this repository."));
    }


    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldDownloadProxyRepositoryIndex(@MavenRepository(repositoryId = REPOSITORY_RELEASES_4,
                                                                    setup = MavenIndexedRepositorySetup.class)
                                                   Repository repository,
                                                   @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_4,
                                                                      id = "org.carlspring.strongbox.indexes:strongbox-test",
                                                                      versions = { "1.0",
                                                                                   "1.1" },
                                                                      classifiers = { "javadoc",
                                                                                      "sources" })
                                                   List<Path> artifactPaths,
                                                   @MavenRepository(repositoryId = REPOSITORY_PROXY_4)
                                                   @Remote(url = PROXY_4_REPOSITORY_URL)
                                                   Repository proxyRepository)
            throws IOException
    {
        hostedRepositoryIndexCreator.apply(repository);
        proxyRepositoryIndexCreator.apply(proxyRepository);

        String url = getContextBaseUrl() + "/storages/" + STORAGE0 + "/" + REPOSITORY_PROXY_4 +
                     "/.index/nexus-maven-repository-index.gz";

        mockMvc.header(new Header("User-Agent", "Maven/*"))
               .get(url)
               .then()
               .log().status()
               .log().headers()
               .statusCode(HttpStatus.OK.value())
               .contentType(APPLICATION_X_GZIP_VALUE)
               .body(CoreMatchers.notNullValue());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void shouldDownloadProxyRepositoryIndexPropertiesFile(@MavenRepository(repositoryId = REPOSITORY_RELEASES_5,
                                                                                  setup = MavenIndexedRepositorySetup.class)
                                                                 Repository repository,
                                                                 @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_5,
                                                                                    id = "org.carlspring.strongbox.indexes:strongbox-test",
                                                                                    versions = { "1.0",
                                                                                                 "1.1" },
                                                                                    classifiers = { "javadoc",
                                                                                                    "sources" })
                                                                 List<Path> artifactPaths,
                                                                 @MavenRepository(repositoryId = REPOSITORY_PROXY_5)
                                                                 @Remote(url = PROXY_5_REPOSITORY_URL)
                                                                 Repository proxyRepository)
            throws IOException
    {
        hostedRepositoryIndexCreator.apply(repository);
        proxyRepositoryIndexCreator.apply(proxyRepository);

        String url = getContextBaseUrl() + "/storages/" + STORAGE0 + "/" + REPOSITORY_PROXY_5 +
                     "/.index/nexus-maven-repository-index.properties";

        mockMvc.header(new Header("User-Agent", "Maven/*"))
               .get(url)
               .then()
               .log().status()
               .log().headers()
               .statusCode(HttpStatus.OK.value())
               .contentType(MediaType.TEXT_PLAIN_VALUE)
               .body(CoreMatchers.notNullValue());
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class,
                  ArtifactManagementTestExecutionListener.class })
    @Test
    public void groupRepositoryIndexShouldBeMergedOnDemand(@MavenRepository(repositoryId = REPOSITORY_RELEASES_6,
                                                                            setup = MavenIndexedRepositorySetup.class)
                                                           Repository repository,
                                                           @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_6,
                                                                              id = PROPERTIES_INJECTOR_GROUP_ID + ":" +
                                                                                   PROPERTIES_INJECTOR_ARTIFACT_ID,
                                                                              versions = { "1.8" },
                                                                              classifiers = { "javadoc",
                                                                                              "sources" })
                                                           Path artifactPathPropertiesInjector,
                                                           @MavenRepository(repositoryId = REPOSITORY_RELEASES_6_1,
                                                                            setup = MavenIndexedRepositorySetup.class)
                                                           Repository repository61,
                                                           @MavenTestArtifact(repositoryId = REPOSITORY_RELEASES_6_1,
                                                                              id = SLF4J_GROUP_ID + ":" + SLF4J_ARTIFACT_ID,
                                                                              versions = { "1.9" },
                                                                              classifiers = { "javadoc",
                                                                                              "sources" })
                                                           Path artifactPathSlf4j,
                                                           @Group(repositories = { REPOSITORY_RELEASES_6,
                                                                                                  REPOSITORY_RELEASES_6_1 })
                                                           @MavenRepository(repositoryId = REPOSITORY_RELEASES_6_GROUP,
                                                                            setup = MavenIndexedRepositorySetup.class)
                                                           Repository groupRepository)
            throws Exception
    {
        hostedRepositoryIndexCreator.apply(repository);
        hostedRepositoryIndexCreator.apply(repository61);

        RepositoryPath indexPath = repositoryLocalIndexDirectoryPathResolver.resolve(groupRepository).resolve(
                "nexus-maven-repository-index.gz");

        assertThat(indexPath).doesNotExist();

        String url = getContextBaseUrl() + "/api/maven/index/{storageId}/{repositoryId}";

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .post(url, groupRepository.getStorage().getId(), groupRepository.getId())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        indexPath = repositoryLocalIndexDirectoryPathResolver.resolve(groupRepository).resolve(
                "nexus-maven-repository-index.gz");

        assertThat(indexPath).matches(Files::exists);
    }

}
