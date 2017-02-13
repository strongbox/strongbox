package org.carlspring.strongbox.services;

import org.carlspring.maven.commons.io.filters.JarFilenameFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.carlspring.strongbox.util.FileUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author Kate Novik.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ChecksumServiceTest
        extends TestCaseWithArtifactGeneration
{

    private static final File REPOSITORY_BASEDIR_1 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage0/releases");

    private static final File REPOSITORY_BASEDIR_2 = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                              "/storages/storage0/snapshots");

    @org.springframework.context.annotation.Configuration
    @ComponentScan(basePackages = { "org.carlspring.strongbox",
                                    "org.carlspring.logging" })
    public static class SpringConfig
    {

    }

    @Inject
    private ChecksumService checksumService;

    @Inject
    private ArtifactMetadataService artifactMetadataService;


    @Before
    public void setUp()
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        if (!new File(REPOSITORY_BASEDIR_1, "org/carlspring/strongbox/checksum/maven/strongbox-checksum").exists())
        {
            //noinspection ResultOfMethodCallIgnored
            REPOSITORY_BASEDIR_1.mkdirs();

            String ga = "org.carlspring.strongbox.checksum.maven:strongbox-checksum";

            // Create released artifacts
            for (int i = 0; i <= 1; i++)
            {
                createRelease(ga + ":1." + i + ":jar");
            }
        }

        if (!new File(REPOSITORY_BASEDIR_2, "org/carlspring/strongbox/checksum").exists())
        {
            //noinspection ResultOfMethodCallIgnored
            REPOSITORY_BASEDIR_2.mkdirs();

            createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR_2.getAbsolutePath(),
                                              "org.carlspring.strongbox",
                                              "checksum",
                                              "2.0",
                                              "jar",
                                              null,
                                              2);
        }
    }

    @Test
    public void testGenerateMavenChecksumForReleaseArtifact()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        String artifactPath = REPOSITORY_BASEDIR_1 + "/org/carlspring/strongbox/checksum/maven/strongbox-checksum";

        artifactMetadataService.rebuildMetadata("storage0", "releases", "org/carlspring/strongbox/checksum");

        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.sha1"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.sha1"));

        FileUtils.deleteIfExists(new File(artifactPath, "/1.1/strongbox-checksum-1.1.jar.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.1/strongbox-checksum-1.1.jar.sha1"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.1/strongbox-checksum-1.1.pom.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/1.1/strongbox-checksum-1.1.pom.sha1"));

        FileUtils.deleteIfExists(new File(artifactPath, "/maven-metadata.xml.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/maven-metadata.xml.sha1"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5").exists());

        checksumService.regenerateChecksum("storage0", "releases", "org/carlspring/strongbox/checksum/maven", false);

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.sha1").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.sha1").length() > 0);

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.jar.md5").length() > 0);

        assertTrue("The checksum file for pom file doesn't exist!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.sha1").exists());
        assertTrue("The checksum file for pom file is empty!",
                   new File(artifactPath, "/1.0/strongbox-checksum-1.0.pom.md5").length() > 0);

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath, "/1.1/strongbox-checksum-1.1.jar.sha1").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath, "/1.1/strongbox-checksum-1.1.jar.sha1").length() > 0);

        assertTrue("The checksum file for pom file doesn't exist!",
                   new File(artifactPath, "/1.1/strongbox-checksum-1.1.pom.sha1").exists());
        assertTrue("The checksum file for pom file is empty!",
                   new File(artifactPath, "/1.1/strongbox-checksum-1.1.pom.md5").length() > 0);

        assertTrue("The checksum file for metadata file doesn't exist!",
                   new File(artifactPath, "/maven-metadata.xml.md5").exists());
        assertTrue("The checksum file for metadata file is empty!",
                   new File(artifactPath, "/maven-metadata.xml.sha1").length() > 0);
    }

    @Test
    public void testGenerateMavenChecksumForSnapshotArtifact()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        String artifactPath = REPOSITORY_BASEDIR_2 + "/org/carlspring/strongbox/checksum";

        File file = new File(artifactPath, "2.0-SNAPSHOT");
        File[] files = file.listFiles(new JarFilenameFilter());
        Artifact artifact = ArtifactUtils.convertPathToArtifact(files[0].getPath());
        String artifactName = artifact.getVersion();

        artifactMetadataService.rebuildMetadata("storage0", "snapshots", "org/carlspring/strongbox/checksum");

        FileUtils.deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/checksum-" + artifactName + ".jar.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/checksum-" + artifactName + ".jar.sha1"));
        FileUtils.deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/checksum-" + artifactName + ".pom.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/checksum-" + artifactName + ".pom.sha1"));

        FileUtils.deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/2.0-SNAPSHOT/maven-metadata.xml.sha1"));

        FileUtils.deleteIfExists(new File(artifactPath, "/maven-metadata.xml.md5"));
        FileUtils.deleteIfExists(new File(artifactPath, "/maven-metadata.xml.sha1"));

        assertTrue("The checksum file for artifact exist!",
                   !new File(artifactPath, "/2.0-SNAPSHOT/checksum-" + artifactName + ".jar.md5").exists());

        checksumService.regenerateChecksum("storage0", "snapshots", "org/carlspring/strongbox/checksum", false);

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath, "/2.0-SNAPSHOT/checksum-" + artifactName + ".jar.sha1").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath, "/2.0-SNAPSHOT/checksum-" + artifactName + ".jar.sha1").length() > 0);

        assertTrue("The checksum file for artifact doesn't exist!",
                   new File(artifactPath, "/2.0-SNAPSHOT/checksum-" + artifactName + ".jar.md5").exists());
        assertTrue("The checksum file for artifact is empty!",
                   new File(artifactPath, "/2.0-SNAPSHOT/checksum-" + artifactName + ".jar.md5").length() > 0);

        assertTrue("The checksum file for pom file doesn't exist!",
                   new File(artifactPath, "/2.0-SNAPSHOT/checksum-" + artifactName + ".pom.sha1").exists());
        assertTrue("The checksum file for pom file is empty!",
                   new File(artifactPath, "/2.0-SNAPSHOT/checksum-" + artifactName + ".pom.md5").length() > 0);

        assertTrue("The checksum file for metadata file doesn't exist!",
                   new File(artifactPath, "/maven-metadata.xml.md5").exists());
        assertTrue("The checksum file for metadata file is empty!",
                   new File(artifactPath, "/maven-metadata.xml.sha1").length() > 0);
    }

    /**
     * Generate a released artifact.
     *
     * @param gavtc String
     * @throws NoSuchAlgorithmException
     * @throws XmlPullParserException
     * @throws IOException
     */
    private Artifact createRelease(String gavtc)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        return generateArtifact(REPOSITORY_BASEDIR_1.getAbsolutePath(), gavtc);
    }

}
