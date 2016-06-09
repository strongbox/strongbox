package org.carlspring.strongbox.storage.repository.artifact.locator;

import org.carlspring.strongbox.artifact.locator.ArtifactDirectoryLocator;
import org.carlspring.strongbox.artifact.locator.handlers.ArtifactLocationReportOperation;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * @author mtodorov
 */
public class ArtifactDirectoryLocatorTest
        extends TestCaseWithArtifactGeneration
{

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases");

    private ByteArrayOutputStream os;

    private static PrintStream tempSysOut;

    private boolean INITIALIZED = false;


    @Before
    public void setUp()
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        if (!new File(REPOSITORY_BASEDIR, "org/carlspring/strongbox/locator").exists())
        {
            //noinspection ResultOfMethodCallIgnored
            REPOSITORY_BASEDIR.mkdirs();

            generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), "org.apache.maven:location-utils", new String[] { "1.0.1", "1.0.2", "1.1", "1.2", "1.2.1" });
            generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), "com.carlspring.strongbox:foo", new String[] { "5.1", "5.2", "5.3" });
            generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), "org.carlspring.maven:locator-testing", new String[] { "2.1", "2.2", "2.3", "2.4", "2.5", "3.0" });
            generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), "org.carlspring.strongbox:locator", new String[] { "5.2.1", "5.2.2", "5.2.2" });
            generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), "org.carlspring.strongbox.locator:foo-locator", new String[] { "1.0", "1.1", "1.2" });
            generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), "org.carlspring.strongbox.locator:utils", new String[] { "2.1", "2.2", "2.3" });
        }

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
        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setBasedir(REPOSITORY_BASEDIR.getAbsolutePath());
        locator.setOperation(new ArtifactLocationReportOperation());
        //! locator.locateArtifactDirectories("/java/nexus/sonatype-work/nexus/storage");
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
        ArtifactDirectoryLocator locator = new ArtifactDirectoryLocator();
        locator.setBasedir(REPOSITORY_BASEDIR.getAbsolutePath());
        locator.setOperation(new ArtifactLocationReportOperation("org/carlspring"));
        locator.locateArtifactDirectories();

        os.flush();

        String output = new String(os.toByteArray());

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
