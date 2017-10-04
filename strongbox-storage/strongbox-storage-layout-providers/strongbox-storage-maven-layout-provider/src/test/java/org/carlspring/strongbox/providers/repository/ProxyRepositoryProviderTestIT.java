package org.carlspring.strongbox.providers.repository;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.Maven2LayoutProviderCronTasksTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.search.SearchException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.services.ArtifactResolutionService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderCronTasksTestConfig.class)
public class ProxyRepositoryProviderTestIT
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final Logger logger = LoggerFactory.getLogger(ProxyRepositoryProviderTestIT.class);

    @Inject
    private ArtifactResolutionService artifactResolutionService;

    @Before
    public void setUp()
            throws Exception
    {
        File derbyPluginBaseDir = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                           "/storages/storage-common-proxies/maven-central/" +
                                           "org/carlspring/maven/derby-maven-plugin");
        if (derbyPluginBaseDir.exists())
        {
            FileUtils.deleteDirectory(derbyPluginBaseDir);
        }
    }

    @Test
    public void shouldBeAbleToProvideFilesFromOracleMavenRepoWithHttpsAndAuthenticationAndRedirections()
            throws ProviderImplementationException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   IOException
    {

        String providedTestOracleRepoUser = System.getProperty("strongbox.test.oracle.repo.user");
        String providedTestOracleRepoPassword = System.getProperty("strongbox.test.oracle.repo.password");

        if (providedTestOracleRepoUser == null || providedTestOracleRepoPassword == null)
        {
            logger.info(
                    "System property strongbox.test.oracle.repo.user or strongbox.test.oracle.repo.password not found. Ignoring test.");
            return;
        }

        RemoteRepository mavenOracleRepository = configurationManagementService.getConfiguration()
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

    @Test
    public void whenDownloadingArtifactMetadaFileShouldAlsoBeResolved()
            throws ProviderImplementationException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   IOException,
                   SearchException,
                   InterruptedException
    {
        String storageId = "storage-common-proxies";
        String repositoryId = "maven-central";

        assertStreamNotNull(storageId, repositoryId,
                            "org/carlspring/properties-injector/1.7/properties-injector-1.7.jar");

        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

        assertTrue(layoutProvider.containsPath(repository, "org/carlspring/properties-injector/maven-metadata.xml"));
    }

    @Test
    public void testMavenCentral()
            throws ProviderImplementationException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   IOException,
                   SearchException,
                   InterruptedException
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

    private void assertStreamNotNull(String storageId,
                                     String repositoryId,
                                     String path)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        InputStream is = artifactResolutionService.getInputStream(storageId, repositoryId, path);

        assertNotNull("Failed to resolve " + path + "!", is);

        if (ArtifactUtils.isMetadata(path))
        {
            System.out.println(ByteStreams.toByteArray(is));
        }

    }

    @Ignore // Broken while Docker is being worked on, as there is no running instance of the Strongbox service.
    @Test
    public void testStrongboxAtCarlspringDotOrg()
            throws ProviderImplementationException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   IOException
    {
        InputStream is = artifactResolutionService.getInputStream("public",
                                                                  "public-group",
                                                                  "org/carlspring/commons/commons-io/1.0-SNAPSHOT/maven-metadata.xml");

        assertNotNull("Failed to resolve org/carlspring/commons/commons-io/1.0-SNAPSHOT/maven-metadata.xml!", is);

        System.out.println(ByteStreams.toByteArray(is));
    }

}
