package org.carlspring.strongbox.artifact.locator;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;


import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.artifact.locator.handlers.ArtifactLocationReportOperation;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.util.TestFileUtils;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class ArtifactDirectoryLocatorTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                            "/storages/storage0/releases");
    
    private ByteArrayOutputStream os;

    private static PrintStream tempSysOut;

    @Before
    public void setUp()
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        os = new ByteArrayOutputStream();
        System.setOut(new PrintStream(os));

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

        tempSysOut = System.out;
    }

    @After
    public void tearDown()
            throws Exception
    {
        resetOutput();
        removeGeneratedArtifacts();
    }

    private void removeGeneratedArtifacts()
    {
        generatedArtifacts.stream().forEach(TestFileUtils::deleteIfExists);
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
        Storage storage = configurationManagementService.getConfiguration().getStorage(STORAGE0);
        Repository repository = storage.getRepository("releases");
        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);
        
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
        Storage storage = configurationManagementService.getConfiguration().getStorage(STORAGE0);
        Repository repository = storage.getRepository("releases");
        
        RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);
        
        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setBasedir(repositoryPath);
        locator.setOperation(new ArtifactLocationReportOperation(repositoryPath.resolve("org/carlspring").relativize()));
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
