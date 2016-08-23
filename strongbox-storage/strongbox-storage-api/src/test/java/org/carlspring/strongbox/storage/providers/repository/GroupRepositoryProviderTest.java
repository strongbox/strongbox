package org.carlspring.strongbox.storage.providers.repository;

import org.carlspring.strongbox.config.CommonConfig;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.config.StorageApiConfig;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.repository.GroupRepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProvider;
import org.carlspring.strongbox.providers.repository.RepositoryProviderRegistry;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import com.carmatechnologies.commons.testing.logging.ExpectedLogs;
import com.carmatechnologies.commons.testing.logging.api.LogLevel;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class GroupRepositoryProviderTest
        extends TestCaseWithArtifactGeneration
{

    @org.springframework.context.annotation.Configuration
    @Import({
            StorageApiConfig.class,
            CommonConfig.class
    })
    public static class SpringConfig { }

    private static final File STORAGE_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0");

    private static final File REPOSITORY_BASEDIR_RELEASES_TEST_NESTED = new File(STORAGE_BASEDIR, "releases-test-nested");
    
    private static final File REPOSITORY_BASEDIR_RELEASES = new File(STORAGE_BASEDIR, "releases");

    private static final File REPOSITORY_BASEDIR_RELEASES_WITH_TRASH = new File(STORAGE_BASEDIR, "releases-with-trash");

    @Autowired
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Autowired
    private ConfigurationManager configurationManager;

    @Rule
    public final ExpectedLogs logs = new ExpectedLogs()
    {{
        captureFor(GroupRepositoryProvider.class, LogLevel.DEBUG);
    }};

    public static boolean INITIALIZED;


    @Before
    public void setUp()
            throws Exception
    {
        if (!INITIALIZED)
        {
            generateArtifact(REPOSITORY_BASEDIR_RELEASES_WITH_TRASH.getAbsolutePath(), "com.artifacts.in.releases.with.trash:foo:1.2.3");
            generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(), "com.artifacts.in.releases:foo:1.2.4");
            generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(), "com.artifacts.denied.in.memory:foo:1.2.5");
            generateArtifact(REPOSITORY_BASEDIR_RELEASES.getAbsolutePath(), "com.artifacts.denied.by.wildcard:foo:1.2.6");

            generateArtifact(REPOSITORY_BASEDIR_RELEASES_TEST_NESTED.getAbsolutePath(), "org.carlspring.metadata.by.juan:juancho:1.2.64");
 
            INITIALIZED = true;
        }
    }

    @After
    public void tearDown()
            throws Exception
    {
        Repository repository = configurationManager.getRepository("storage0:releases");
        if (!repository.isInService())
        {
            repository.putInService();
        }
    }

    @Test
    public void testGroupIncludes()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group includes...");

        Repository repository = configurationManager.getRepository("storage0:group-releases");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream("storage0",
                                                           "group-releases",
                                                           "com/artifacts/in/releases/with/trash/foo/1.2.3/foo-1.2.3.jar");

        assertNotNull(is);

        ResourceCloser.close(is, null);
    }

    @Test
    public void testGroupIncludesWithOutOfServiceRepository()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group includes with out of service repository...");

        configurationManager.getConfiguration().getStorage("storage0")
                            .getRepository("releases").putOutOfService();

        Repository repository = configurationManager.getRepository("storage0:group-releases");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream("storage0",
                                                           "group-releases",
                                                           "com/artifacts/in/releases/foo/1.2.4/foo-1.2.4.jar");

        assertNull(is);

        ResourceCloser.close(is, null);
    }

    @Test
    public void testGroupIncludesWildcardRule()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group includes with wildcard...");

        Repository repository = configurationManager.getRepository("storage0:group-releases");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream("storage0",
                                                           "group-releases",
                                                           "com/artifacts/in/releases/foo/1.2.4/foo-1.2.4.jar");

        assertNotNull(is);

        ResourceCloser.close(is, null);
    }

    @Test
    public void testGroupIncludesWildcardRuleAgainstNestedRepository()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group includes with wildcard against nested repositories...");

        Repository repository = configurationManager.getRepository("storage0:group-releases-nested");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream("storage0",
                                                           "group-releases-nested",
                                                           "com/artifacts/in/releases/foo/1.2.4/foo-1.2.4.jar");

        assertNotNull(is);

        ResourceCloser.close(is, null);
    }
    
    @Test
    public void testGroupAgainstNestedRepository()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group includes with wildcard against nested repositories...");

        Repository repository = configurationManager.getRepository("storage0:group-releases-nested-deep-1");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream("storage0",
                                                           "group-releases-nested-deep-1",
                                                           "org/carlspring/metadata/by/juan/juancho/1.2.64/juancho-1.2.64.jar");

        assertNotNull(is);

        ResourceCloser.close(is, null);
    }

    @Test
    public void testGroupExcludes()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group excludes...");

        Repository repository = configurationManager.getRepository("storage0:group-releases");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream("storage0",
                                                           "group-releases",
                                                           "com/artifacts/denied/in/memory/foo/1.2.5/foo-1.2.5.jar");

        assertNull(is);
    }

    @Test
    public void testGroupExcludesWildcardRule()
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        System.out.println("# Testing group excludes with wildcard...");

        Repository repository = configurationManager.getRepository("storage0:group-releases");
        RepositoryProvider repositoryProvider = repositoryProviderRegistry.getProvider(repository.getType());

        InputStream is = repositoryProvider.getInputStream("storage0",
                                                           "group-releases",
                                                           "com/artifacts/denied/by/wildcard/foo/1.2.6/foo-1.2.6.jar");

        assertNull(is);
    }

}
