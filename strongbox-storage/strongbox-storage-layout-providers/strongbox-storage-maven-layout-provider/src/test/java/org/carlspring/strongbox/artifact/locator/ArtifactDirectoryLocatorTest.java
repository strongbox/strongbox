package org.carlspring.strongbox.artifact.locator;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;

import javax.inject.Inject;

import org.carlspring.strongbox.artifact.locator.handlers.ArtifactLocationReportOperation;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.storage.StorageProviderRegistry;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ArtifactDirectoryLocatorTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                            "/storages/storage0/releases");

    private static boolean initialized;
    
    private ByteArrayOutputStream os;

    private static PrintStream tempSysOut;

    private boolean INITIALIZED;
    
    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;
    @Inject
    private StorageProviderRegistry storageProviderRegistry;

    @Before
    public void setUp()
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        if (initialized)
        {
            return;
        }

        initialized = true;
        
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/apache/maven/location-utils/1.0.1/location-utils-1.0.1.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/apache/maven/location-utils/1.0.1/location-utils-1.0.1.pom");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/apache/maven/location-utils/1.0.2/location-utils-1.0.2.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/apache/maven/location-utils/1.0.2/location-utils-1.0.2.pom");
        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/apache/maven/location-utils/1.1/location-utils-1.1.jar");
        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/apache/maven/location-utils/1.1/location-utils-1.1.pom");
        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/apache/maven/location-utils/1.2/location-utils-1.2.jar");
        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/apache/maven/location-utils/1.2/location-utils-1.2.pom");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/apache/maven/location-utils/1.2.1/location-utils-1.2.1.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/apache/maven/location-utils/1.2.1/location-utils-1.2.1.pom");

        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() + "/com/carlspring/strongbox/foo/5.1/foo-5.1.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() + "/com/carlspring/strongbox/foo/5.1/foo-5.1.pom");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() + "/com/carlspring/strongbox/foo/5.2/foo-5.2.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() + "/com/carlspring/strongbox/foo/5.2/foo-5.2.pom");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() + "/com/carlspring/strongbox/foo/5.3/foo-5.3.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() + "/com/carlspring/strongbox/foo/5.3/foo-5.3.pom");

        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/maven/locator-testing/2.1/locator-testing-2.1.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/maven/locator-testing/2.1/locator-testing-2.1.pom");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/maven/locator-testing/2.2/locator-testing-2.2.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/maven/locator-testing/2.2/locator-testing-2.2.pom");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/maven/locator-testing/2.3/locator-testing-2.3.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/maven/locator-testing/2.3/locator-testing-2.3.pom");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/maven/locator-testing/2.4/locator-testing-2.4.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/maven/locator-testing/2.4/locator-testing-2.4.pom");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/maven/locator-testing/2.5/locator-testing-2.5.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/maven/locator-testing/2.5/locator-testing-2.5.pom");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/maven/locator-testing/3.0/locator-testing-3.0.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/maven/locator-testing/3.0/locator-testing-3.0.pom");

        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/carlspring/strongbox/locator/5.2.1/locator-5.2.1.jar");
        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/carlspring/strongbox/locator/5.2.1/locator-5.2.1.pom");
        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/carlspring/strongbox/locator/5.2.2/locator-5.2.2.jar");
        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/carlspring/strongbox/locator/5.2.2/locator-5.2.2.pom");

        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/strongbox/locator/foo-locator/1.0/foo-locator-1.0.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/strongbox/locator/foo-locator/1.0/foo-locator-1.0.pom");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/strongbox/locator/foo-locator/1.1/foo-locator-1.1.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/strongbox/locator/foo-locator/1.1/foo-locator-1.1.pom");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/strongbox/locator/foo-locator/1.2/foo-locator-1.2.jar");
        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath() +
                         "/org/carlspring/strongbox/locator/foo-locator/1.2/foo-locator-1.2.pom");

        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/carlspring/strongbox/locator/utils/2.1/utils-2.1.jar");
        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/carlspring/strongbox/locator/utils/2.1/utils-2.1.pom");
        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/carlspring/strongbox/locator/utils/2.2/utils-2.2.jar");
        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/carlspring/strongbox/locator/utils/2.2/utils-2.2.pom");
        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/carlspring/strongbox/locator/utils/2.3/utils-2.3.jar");
        generateArtifact(
                REPOSITORY_BASEDIR.getAbsolutePath() + "/org/carlspring/strongbox/locator/utils/2.3/utils-2.3.pom");

        if (!INITIALIZED)
        {
            tempSysOut = System.out;

            INITIALIZED = true;
        }

        os = new ByteArrayOutputStream();
        System.setOut(new PrintStream(os));
    }

    @After
    public void tearDown()
            throws Exception
    {
        resetOutput();
    }

    private void resetOutput()
    {
        os = null;
        System.setOut(tempSysOut);
    }

    @Test
    public void testLocateDirectories()
            throws IOException
    {
        Storage storage = storageProviderRegistry.getStorage(STORAGE0);
        Repository repository = storage.getRepository("releases");
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        RepositoryPath repositoryPath = layoutProvider.resolve(repository);
        
        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setBasedir(repositoryPath);
        locator.setOperation(new ArtifactLocationReportOperation());
        locator.locateArtifactDirectories();

        os.flush();

        String output = new String(os.toByteArray());

        assertTrue(output.contains(normalize("org/apache/maven/location-utils")));
        assertTrue(output.contains(normalize("org/carlspring/maven/locator-testing")));
        assertTrue(output.contains(normalize("org/carlspring/strongbox/locator/foo-locator")));
        assertTrue(output.contains(normalize("org/apache/maven/location-utils")));
        assertTrue(output.contains(normalize("org/carlspring/strongbox/locator/utils")));

        // resetOutput();
        // System.out.println(output);
    }

    @Test
    public void testLocateDirectoriesWithBasePath()
            throws IOException
    {
        Storage storage = storageProviderRegistry.getStorage(STORAGE0);
        Repository repository = storage.getRepository("releases");
        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        RepositoryPath repositoryPath = layoutProvider.resolve(repository);
        
        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setBasedir(repositoryPath);
        locator.setOperation(new ArtifactLocationReportOperation(repositoryPath.resolve("org/carlspring").getRepositoryRelative()));
        locator.locateArtifactDirectories();

        os.flush();

        String output = new String(os.toByteArray());

        System.out.println(output);

        assertFalse(output.contains(normalize("org/apache/maven/location-utils")));
        assertTrue(output.contains(normalize("org/carlspring/maven/locator-testing")));
        assertTrue(output.contains(normalize("org/carlspring/strongbox/locator/foo-locator")));
        assertTrue(output.contains(normalize("org/carlspring/strongbox/locator/utils")));

        resetOutput();

        System.out.println(output);
    }

    private String normalize(String path)
    {
        if (!File.separator.equals("/"))
        {
            path = path.replaceAll("/", Matcher.quoteReplacement(System.getProperty("file.separator")));
        }

        return path;
    }

}
