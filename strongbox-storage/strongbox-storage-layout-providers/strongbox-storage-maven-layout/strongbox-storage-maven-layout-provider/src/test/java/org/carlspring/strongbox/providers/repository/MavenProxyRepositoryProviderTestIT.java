package org.carlspring.strongbox.providers.repository;


import org.carlspring.strongbox.testing.storage.repository.TestRepository.Remote;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.MavenIndexedRepositorySetup;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.carlspring.strongbox.testing.repository.MavenRepository;
import org.carlspring.strongbox.testing.storage.repository.RepositoryManagementTestExecutionListener;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.CONCURRENT;

/**
 * @author carlspring
 */
@SpringBootTest
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
@TestExecutionListeners(listeners = { CacheManagerTestExecutionListener.class },
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
@Execution(CONCURRENT)
public class MavenProxyRepositoryProviderTestIT
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String STORAGE_ID = "storage-common-proxies";

    private static final String REPOSITORY_ID = "spring-libs-release-it";

    private static final String REMOTE_URL = "https://repo.spring.io/libs-release/";

    private static final String CENTRAL_REPOSITORY_ID = "central-release-it";

    private static final String CENTRAL_URL = "http://central.maven.org/maven2/";

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private MavenMetadataManager mavenMetadataManager;

    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {

        final String repositoryId = getRepositoryName(REPOSITORY_ID,
                                                      testInfo);

        final String centralRepositoryId = getRepositoryName(CENTRAL_REPOSITORY_ID,
                                                             testInfo);

        artifactEntryService.delete(
                artifactEntryService.findArtifactList(STORAGE_ID,
                                                      repositoryId,
                                                      ImmutableMap.of("groupId", "org.carlspring.maven",
                                                                      "artifactId", "derby-maven-plugin",
                                                                      "version", "1.10"),
                                                      true));

        artifactEntryService.delete(
                artifactEntryService.findArtifactList(STORAGE_ID,
                                                      repositoryId,
                                                      ImmutableMap.of("groupId", "org.carlspring",
                                                                      "artifactId", "properties-injector",
                                                                      "version", "1.1"),
                                                      true));

        artifactEntryService.delete(
                artifactEntryService.findArtifactList(STORAGE_ID,
                                                      repositoryId,
                                                      ImmutableMap.of("groupId", "javax.media",
                                                                      "artifactId", "jai_core",
                                                                      "version", "1.1.3"),
                                                      true));
    }

    /*
    @Test
    public void shouldBeAbleToProvideFilesFromOracleMavenRepoWithHttpsAndAuthenticationAndRedirections()
            throws Exception
    {

        String providedTestOracleRepoUser = System.getProperty("strongbox.test.oracle.repo.user");
        String providedTestOracleRepoPassword = System.getProperty("strongbox.test.oracle.repo.password");

        if (providedTestOracleRepoUser == null || providedTestOracleRepoPassword == null)
        {
            logger.info(
                    "System property strongbox.test.oracle.repo.user or strongbox.test.oracle.repo.password not found. Ignoring test.");
            return;
        }

        ImmutableRemoteRepository mavenOracleRepository = configurationManagementService.getConfiguration()
                                                                                        .getStorage("storage-common-proxies")
                                                                                        .getRepository("maven-oracle")
                                                                                        .getRemoteRepository();

        String initialUsername = mavenOracleRepository.getUsername();
        String initialPassword = mavenOracleRepository.getPassword();

        mavenOracleRepository.setUsername(providedTestOracleRepoUser);
        mavenOracleRepository.setPassword(providedTestOracleRepoPassword);

        assertStreamNotNull("storage-common-proxies",
                            "maven-oracle",
                            "com/oracle/jdbc/ojdbc8/12.2.0.1/ojdbc8-12.2.0.1.jar");

        assertStreamNotNull("storage-common-proxies",
                            "maven-oracle",
                            "com/oracle/jdbc/ojdbc8/12.2.0.1/ojdbc8-12.2.0.1.pom");

        mavenOracleRepository.setUsername(initialUsername);
        mavenOracleRepository.setPassword(initialPassword);
    }
    */

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void whenDownloadingArtifactMetadaFileShouldAlsoBeResolved(@MavenRepository(storageId = STORAGE_ID,
                                                                                       repositoryId = REPOSITORY_ID + "-whenDownloadingArtifactMetadaFileShouldAlsoBeResolved",
                                                                                       setup = MavenIndexedRepositorySetup.class)
                                                                      @Remote(url = REMOTE_URL)
                                                                      Repository proxyRepository)
            throws Exception
    {

        String storageId = proxyRepository.getStorage().getId();
        String repositoryId = proxyRepository.getId();

        assertStreamNotNull(storageId,
                            repositoryId,
                            "org/carlspring/properties-injector/1.1/properties-injector-1.1.jar");

        assertTrue(RepositoryFiles.artifactExists(
                repositoryPathResolver.resolve(proxyRepository,
                                               "org/carlspring/properties-injector/maven-metadata.xml")));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void whenDownloadingArtifactMetadataFileShouldBeMergedWhenExist(@MavenRepository(storageId = STORAGE_ID,
                                                                                            repositoryId = CENTRAL_REPOSITORY_ID + "-whenDownloadingArtifactMetadataFileShouldBeMergedWhenExist",
                                                                                            setup = MavenIndexedRepositorySetup.class)
                                                                           @Remote(url = CENTRAL_URL)
                                                                           Repository proxyRepository1,
                                                                           @MavenRepository(storageId = STORAGE_ID,
                                                                                            repositoryId = REPOSITORY_ID + "-whenDownloadingArtifactMetadataFileShouldBeMergedWhenExist",
                                                                                            setup = MavenIndexedRepositorySetup.class)
                                                                           @Remote(url = REMOTE_URL)
                                                                           Repository proxyRepository2)
            throws Exception
    {
        String storageId = proxyRepository1.getStorage().getId();

        // 1. download the artifact and artifactId-level maven metadata-file from 1st repository
        String repositoryId = proxyRepository1.getId();

        assertStreamNotNull(storageId,
                            repositoryId,
                            "javax/interceptor/javax.interceptor-api/1.2.2/javax.interceptor-api-1.2.2.jar");

        // 2. resolve downloaded artifact base path
        //Repository repository = storage.getRepository(repositoryId);
        final Path mavenCentralArtifactBaseBath = repositoryPathResolver.resolve(proxyRepository1,
                                                                                 "javax/interceptor/javax.interceptor-api");

        // 3. copy the content to 2nd repository
        repositoryId = proxyRepository2.getId();

        final Path secondRepoArtifactBaseBath = repositoryPathResolver.resolve(proxyRepository2,
                                                                               "javax/interceptor/javax.interceptor-api");
        FileUtils.copyDirectory(mavenCentralArtifactBaseBath.toFile(),
                                secondRepoArtifactBaseBath.toFile());

        // 4. confirm maven-metadata.xml lies in the 2nd repository
        assertTrue(RepositoryFiles.artifactExists(
                repositoryPathResolver.resolve(proxyRepository2,
                                               "javax/interceptor/javax.interceptor-api/maven-metadata.xml")));

        // 5. confirm some pre-merge state
        Path artifactBasePath = repositoryPathResolver.resolve(proxyRepository2,
                                                               "javax/interceptor/javax.interceptor-api/");
        Metadata metadata = mavenMetadataManager.readMetadata(artifactBasePath);
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(9));
        assertThat(metadata.getVersioning().getVersions().get(8), CoreMatchers.equalTo("1.2.2"));

        // 6. download the artifact from remote 2nd repository - it contains different maven-metadata.xml file
        assertStreamNotNull(storageId,
                            repositoryId,
                            "javax/interceptor/javax.interceptor-api/1.2.2/javax.interceptor-api-1.2.2.jar");

        // 7. confirm the state of maven-metadata.xml file has changed
        metadata = mavenMetadataManager.readMetadata(artifactBasePath);
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(10));
        assertThat(metadata.getVersioning().getVersions().get(9), CoreMatchers.equalTo("3.1"));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void whenDownloadingArtifactDatabaseShouldBeAffectedByArtifactEntry(@MavenRepository(storageId = STORAGE_ID,
                                                                                                repositoryId = REPOSITORY_ID + "-whenDownloadingArtifactDatabaseShouldBeAffectedByArtifactEntry",
                                                                                                setup = MavenIndexedRepositorySetup.class)
                                                                               @Remote(url = REMOTE_URL)
                                                                               Repository proxyRepository)
            throws Exception
    {
        String storageId = proxyRepository.getStorage().getId();
        String repositoryId = proxyRepository.getId();
        String path = "org/carlspring/properties-injector/1.6/properties-injector-1.6.jar";

        Optional<ArtifactEntry> artifactEntry = Optional.ofNullable(artifactEntryService.findOneArtifact(storageId,
                                                                                                         repositoryId,
                                                                                                         path));
        assertThat(artifactEntry, CoreMatchers.equalTo(Optional.empty()));

        assertStreamNotNull(storageId,
                            repositoryId,
                            path);

        artifactEntry = Optional.ofNullable(artifactEntryService.findOneArtifact(storageId,
                                                                                 repositoryId,
                                                                                 path));
        assertThat(artifactEntry, CoreMatchers.not(CoreMatchers.equalTo(Optional.empty())));
    }

    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testMavenCentral(@MavenRepository(storageId = STORAGE_ID,
                                                  repositoryId = REPOSITORY_ID + "-testMavenCentral",
                                                  setup = MavenIndexedRepositorySetup.class)
                                 @Remote(url = REMOTE_URL)
                                 Repository proxyRepository)
            throws Exception
    {
        String storageId = proxyRepository.getStorage().getId();
        String repositoryId = proxyRepository.getId();

        assertStreamNotNull(storageId,
                            repositoryId,
                            "org/carlspring/maven/derby-maven-plugin/maven-metadata.xml");
        assertStreamNotNull(storageId,
                            repositoryId,
                            "org/carlspring/maven/derby-maven-plugin/maven-metadata.xml.md5");
        assertStreamNotNull(storageId,
                            repositoryId,
                            "org/carlspring/maven/derby-maven-plugin/maven-metadata.xml.sha1");

        assertStreamNotNull(storageId,
                            repositoryId,
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.pom");
        assertStreamNotNull(storageId,
                            repositoryId,
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.pom.md5");
        assertStreamNotNull(storageId,
                            repositoryId,
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.pom.sha1");

        assertStreamNotNull(storageId,
                            repositoryId,
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar");
        assertStreamNotNull(storageId,
                            repositoryId,
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar.md5");
        assertStreamNotNull(storageId,
                            repositoryId,
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar.sha1");

        assertIndexContainsArtifact(storageId,
                                    repositoryId,
                                    "+g:org.carlspring.maven +a:derby-maven-plugin +v:1.10");
    }

    @Disabled // Broken while Docker is being worked on, as there is no running instance of the Strongbox service.
    @ExtendWith({ RepositoryManagementTestExecutionListener.class })
    @Test
    public void testStrongboxAtCarlspringDotOrg()
            throws IOException
    {
        RepositoryPath path = artifactResolutionService.resolvePath("public",
                                                                    "maven-group",
                                                                    "org/carlspring/commons/commons-io/1.0-SNAPSHOT/maven-metadata.xml");
        try (InputStream is = artifactResolutionService.getInputStream(path))
        {

            assertNotNull(is,
                          "Failed to resolve org/carlspring/commons/commons-io/1.0-SNAPSHOT/maven-metadata.xml!");
            System.out.println(ByteStreams.toByteArray(is));
        }
    }

}
