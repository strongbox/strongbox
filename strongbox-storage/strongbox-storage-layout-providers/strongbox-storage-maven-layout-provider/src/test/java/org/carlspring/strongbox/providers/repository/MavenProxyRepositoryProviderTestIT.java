package org.carlspring.strongbox.providers.repository;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.metadata.MavenMetadataManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
public class MavenProxyRepositoryProviderTestIT
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final Logger logger = LoggerFactory.getLogger(MavenProxyRepositoryProviderTestIT.class);

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private MavenMetadataManager mavenMetadataManager;

    @Before
    @After
    public void cleanup()
            throws Exception
    {
        deleteDirectoryRelativeToVaultDirectory(
                "storages/storage-common-proxies/maven-central/org/carlspring/maven/derby-maven-plugin");
        deleteDirectoryRelativeToVaultDirectory("storages/storage-common-proxies/maven-oracle/com/oracle/jdbc/ojdbc8");
        deleteDirectoryRelativeToVaultDirectory(
                "storages/storage-common-proxies/maven-central/org/carlspring/properties-injector");
        deleteDirectoryRelativeToVaultDirectory("storages/storage-common-proxies/maven-central/javax/media/jai_core");

        artifactEntryService.deleteAll();
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
    public void whenDownloadingArtifactMetadaFileShouldAlsoBeResolved()
            throws Exception
    {
        String storageId = "storage-common-proxies";
        String repositoryId = "maven-central";

        assertStreamNotNull(storageId, repositoryId,
                            "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");

        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        assertTrue(layoutProvider.containsPath(repositoryPathResolver.resolve(repository, "org/carlspring/properties-injector/maven-metadata.xml")));
    }

    @Test
    public void whenDownloadingArtifactMetadataFileShouldBeMergedWhenExist()
            throws Exception
    {
        String storageId = "storage-common-proxies";
        Storage storage = getConfiguration().getStorage(storageId);

        // 1. download the artifact and artifactId-level maven metadata-file from maven-central
        String repositoryId = "maven-central";
        assertStreamNotNull(storageId, repositoryId, "javax/media/jai_core/1.1.3/jai_core-1.1.3-javadoc.jar");

        // 2. resolve downloaded artifact base path
        Repository repository = storage.getRepository(repositoryId);
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final Path mavenCentralArtifactBaseBath = repositoryPathResolver.resolve(repository, "javax/media/jai_core");

        // 3. copy the content to carlspring repository
        repositoryId = "carlspring";
        repository = storage.getRepository(repositoryId);
        final Path carlspringArtifactBaseBath = repositoryPathResolver.resolve(repository).resolve("javax/media/jai_core");
        FileUtils.copyDirectory(mavenCentralArtifactBaseBath.toFile(), carlspringArtifactBaseBath.toFile());

        // 4. confirm maven-metadata.xml lies in the carlspring repository
        assertTrue(layoutProvider.containsPath(repositoryPathResolver.resolve(repository).resolve("javax/media/jai_core/maven-metadata.xml")));

        // 5. confirm some pre-merge state
        Path artifactBasePath = repositoryPathResolver.resolve(repository).resolve("javax/media/jai_core/");
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
    public void whenDownloadingArtifactDatabaseShouldBeAffectedByArtifactEntry()
            throws Exception
    {
        String storageId = "storage-common-proxies";
        String repositoryId = "maven-central";
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
    public void testMavenCentral()
            throws Exception
    {
        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/maven-metadata.xml");
        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/maven-metadata.xml.md5");
        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/maven-metadata.xml.sha1");

        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.pom");
        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.pom.md5");
        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.pom.sha1");

        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar");
        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar.md5");
        assertStreamNotNull("storage-common-proxies",
                            "maven-central",
                            "org/carlspring/maven/derby-maven-plugin/1.10/derby-maven-plugin-1.10.jar.sha1");

        assertIndexContainsArtifact("storage-common-proxies",
                                    "maven-central",
                                    "+g:org.carlspring.maven +a:derby-maven-plugin +v:1.10");
    }

    @Ignore // Broken while Docker is being worked on, as there is no running instance of the Strongbox service.
    @Test
    public void testStrongboxAtCarlspringDotOrg()
            throws ProviderImplementationException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   IOException
    {
        RepositoryPath path = artifactResolutionService.resolvePath("public",
                                                                    "maven-group",
                                                                    "org/carlspring/commons/commons-io/1.0-SNAPSHOT/maven-metadata.xml");
        try (InputStream is = artifactResolutionService.getInputStream(path))
        {

            assertNotNull("Failed to resolve org/carlspring/commons/commons-io/1.0-SNAPSHOT/maven-metadata.xml!", is);
            System.out.println(ByteStreams.toByteArray(is));
        }
    }

}
