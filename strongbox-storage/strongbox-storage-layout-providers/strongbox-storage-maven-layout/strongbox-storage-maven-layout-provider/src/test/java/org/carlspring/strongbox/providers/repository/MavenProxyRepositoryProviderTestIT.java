package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.data.CacheManagerTestExecutionListener;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.*;
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

    private static final String REPOSITORY_ID = "maven-central-proxies-it";

    private static final String REMOTE_URL = "http://maven.ibiblio.org/maven2/";

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private MavenMetadataManager mavenMetadataManager;

    private Set<MutableRepository> getRepositories(TestInfo testInfo)
    {
        Set<MutableRepository> repositories = new LinkedHashSet<>();
        repositories.add(createRepositoryMock(STORAGE_ID, getRepositoryName(REPOSITORY_ID, testInfo),
                                              Maven2LayoutProvider.ALIAS));
        return repositories;
    }

    @AfterEach
    public void removeRepositories(TestInfo testInfo)
            throws Exception
    {
        removeRepositories(getRepositories(testInfo));
    }

    @BeforeEach
    public void init(TestInfo testInfo)
            throws Exception
    {

        final String repositoryId = getRepositoryName(REPOSITORY_ID, testInfo);

        createProxyRepository(STORAGE_ID,
                              repositoryId,
                              REMOTE_URL);

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

    @Test
    public void whenDownloadingArtifactMetadaFileShouldAlsoBeResolved(TestInfo testInfo)
            throws Exception
    {
        String storageId = STORAGE_ID;
        String repositoryId = getRepositoryName(REPOSITORY_ID, testInfo);

        assertStreamNotNull(storageId, repositoryId,
                            "org/carlspring/properties-injector/1.1/properties-injector-1.1.jar");

        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        assertTrue(RepositoryFiles.artifactExists(
                repositoryPathResolver.resolve(repository, "org/carlspring/properties-injector/maven-metadata.xml")));
    }

    @Disabled // Issue 1086: https://github.com/strongbox/strongbox/issues/1086
    @Test
    public void whenDownloadingArtifactMetadataFileShouldBeMergedWhenExist(TestInfo testInfo)
            throws Exception
    {
        String storageId = STORAGE_ID;
        Storage storage = getConfiguration().getStorage(storageId);

        // 1. download the artifact and artifactId-level maven metadata-file from maven-central
        String repositoryId = getRepositoryName(REPOSITORY_ID, testInfo);
        assertStreamNotNull(storageId, repositoryId, "javax/media/jai_core/1.1.3/jai_core-1.1.3-javadoc.jar");

        // 2. resolve downloaded artifact base path
        Repository repository = storage.getRepository(repositoryId);
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final Path mavenCentralArtifactBaseBath = repositoryPathResolver.resolve(repository, "javax/media/jai_core");

        // 3. copy the content to carlspring repository
        repositoryId = "carlspring";
        repository = storage.getRepository(repositoryId);
        final Path carlspringArtifactBaseBath = repositoryPathResolver.resolve(repository, "javax/media/jai_core");
        FileUtils.copyDirectory(mavenCentralArtifactBaseBath.toFile(), carlspringArtifactBaseBath.toFile());

        // 4. confirm maven-metadata.xml lies in the carlspring repository
        assertTrue(RepositoryFiles.artifactExists(
                repositoryPathResolver.resolve(repository, "javax/media/jai_core/maven-metadata.xml")));

        // 5. confirm some pre-merge state
        Path artifactBasePath = repositoryPathResolver.resolve(repository, "javax/media/jai_core/");
        Metadata metadata = mavenMetadataManager.readMetadata(artifactBasePath);
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(1));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.1.2_01"));

        // 6. download the artifact from remote carlspring repository - it contains different maven-metadata.xml file
        assertStreamNotNull(storageId, repositoryId, "javax/media/jai_core/1.1.3/jai_core-1.1.3.jar");

        // 7. confirm the state of maven-metadata.xml file has changed
        metadata = mavenMetadataManager.readMetadata(artifactBasePath);
        assertThat(metadata.getVersioning().getVersions().size(), CoreMatchers.equalTo(2));
        assertThat(metadata.getVersioning().getVersions().get(0), CoreMatchers.equalTo("1.1.2_01"));
        assertThat(metadata.getVersioning().getVersions().get(1), CoreMatchers.equalTo("1.1.3"));
    }

    @Test
    public void whenDownloadingArtifactDatabaseShouldBeAffectedByArtifactEntry(TestInfo testInfo)
            throws Exception
    {
        String storageId = STORAGE_ID;
        String repositoryId = getRepositoryName(REPOSITORY_ID, testInfo);
        String path = "org/carlspring/properties-injector/1.6/properties-injector-1.6.jar";

        Optional<ArtifactEntry> artifactEntry = Optional.ofNullable(artifactEntryService.findOneArtifact(storageId,
                                                                                                         repositoryId,
                                                                                                         path));
        assertThat(artifactEntry, CoreMatchers.equalTo(Optional.empty()));

        assertStreamNotNull(storageId, repositoryId, path);

        artifactEntry = Optional.ofNullable(artifactEntryService.findOneArtifact(storageId, repositoryId, path));
        assertThat(artifactEntry, CoreMatchers.not(CoreMatchers.equalTo(Optional.empty())));
    }

    @Test
    public void testMavenCentral(TestInfo testInfo)
            throws Exception
    {
        String storageId = STORAGE_ID;
        String repositoryId = getRepositoryName(REPOSITORY_ID, testInfo);

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
    @Test
    public void testStrongboxAtCarlspringDotOrg()
            throws IOException
    {
        RepositoryPath path = artifactResolutionService.resolvePath("public",
                                                                    "maven-group",
                                                                    "org/carlspring/commons/commons-io/1.0-SNAPSHOT/maven-metadata.xml");
        try (InputStream is = artifactResolutionService.getInputStream(path))
        {

            assertNotNull(is, "Failed to resolve org/carlspring/commons/commons-io/1.0-SNAPSHOT/maven-metadata.xml!");
            System.out.println(ByteStreams.toByteArray(is));
        }
    }

}
