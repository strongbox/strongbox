package org.carlspring.strongbox.testing.integration;

import org.carlspring.strongbox.artifact.generator.ArtifactDeployer;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.carlspring.maven.commons.util.ArtifactUtils.getArtifactFromGAVTC;
import static org.junit.Assert.assertTrue;

/**
 * @author carlspring
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class LargeArtifactDeploymentTest
        extends RestAssuredBaseTest
{

    private static final File BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                          "/storages/storage0/releases");

    private static final File BASEDIR_SNAPSHOTS = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                           "/storages/storage0/snapshots");


    @Before
    public void setUp()
            throws Exception
    {
        File snapshotsTempDir = new File(BASEDIR_SNAPSHOTS, ".temp");
        if (!snapshotsTempDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            snapshotsTempDir.mkdirs();
        }
        File releasesTempDir = new File(BASEDIR_RELEASES, ".temp");
        if (!releasesTempDir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            releasesTempDir.mkdirs();
        }
    }

    @Test
    public void testWith2GBSnapshotArtifact()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException,
                   ArtifactOperationException,
                   ArtifactTransportException
    {
        File basedir = new File("target/test-resources");
        if (!basedir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            basedir.mkdirs();
        }

        String ga = "org.carlspring.strongbox.tests:large-artifact-2GB";

        String snapshotVersion = createSnapshotVersion("1.0", 1);

        Artifact artifact1WithTimestamp1 = getArtifactFromGAVTC(ga + ":" + snapshotVersion);

        ArtifactDeployer artifactDeployer = buildArtifactDeployer(basedir);

        String storageId = "storage0";
        String repositoryId = "snapshots";

        artifactDeployer.setSize(2000000L); // ~2 MB
        artifactDeployer.generateAndDeployArtifact(artifact1WithTimestamp1, storageId, repositoryId);

        File artifactBaseDir = new File(BASEDIR_SNAPSHOTS + "/org/carlspring/strongbox/tests/large-artifact-2GB");

        assertTrue(new File(artifactBaseDir, "1.0-SNAPSHOT/large-artifact-2GB-" + snapshotVersion + ".jar").exists());
        assertTrue(new File(artifactBaseDir, "1.0-SNAPSHOT/large-artifact-2GB-" + snapshotVersion + ".jar.md5").exists());
        assertTrue(new File(artifactBaseDir, "1.0-SNAPSHOT/large-artifact-2GB-" + snapshotVersion + ".jar.sha1").exists());
        assertTrue(new File(artifactBaseDir, "1.0-SNAPSHOT/large-artifact-2GB-" + snapshotVersion + ".pom").exists());
        assertTrue(new File(artifactBaseDir, "1.0-SNAPSHOT/large-artifact-2GB-" + snapshotVersion + ".pom.md5").exists());
        assertTrue(new File(artifactBaseDir, "1.0-SNAPSHOT/large-artifact-2GB-" + snapshotVersion + ".pom.sha1").exists());
        assertTrue(new File(artifactBaseDir, "1.0-SNAPSHOT/maven-metadata.xml").exists());
        assertTrue(new File(artifactBaseDir, "1.0-SNAPSHOT/maven-metadata.xml.md5").exists());
        assertTrue(new File(artifactBaseDir, "1.0-SNAPSHOT/maven-metadata.xml.sha1").exists());
        assertTrue(new File(artifactBaseDir, "maven-metadata.xml").exists());
        assertTrue(new File(artifactBaseDir, "maven-metadata.xml.md5").exists());
        assertTrue(new File(artifactBaseDir, "maven-metadata.xml.sha1").exists());

        System.out.println(artifactBaseDir.getAbsolutePath());
    }

    @Test
    public void testWith2GBReleaseArtifact()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException,
                   ArtifactOperationException,
                   ArtifactTransportException
    {
        File basedir = new File("target/test-resources");
        if (!basedir.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            basedir.mkdirs();
        }

        String ga = "org.carlspring.strongbox.tests:large-artifact-2GB:1.0";

        Artifact artifact = getArtifactFromGAVTC(ga);

        ArtifactDeployer artifactDeployer = buildArtifactDeployer(basedir);

        String storageId = "storage0";
        String repositoryId = "releases";

        artifactDeployer.setSize(2000000L); // ~2 MB
        artifactDeployer.generateAndDeployArtifact(artifact, storageId, repositoryId);

        File artifactBaseDir = new File(ConfigurationResourceResolver.getVaultDirectory() + "/" +
                                        "storages/storage0/releases/" +
                                        "org/carlspring/strongbox/tests/large-artifact-2GB");

        assertTrue(new File(artifactBaseDir, "1.0/large-artifact-2GB-1.0.jar").exists());
        assertTrue(new File(artifactBaseDir, "1.0/large-artifact-2GB-1.0.jar.md5").exists());
        assertTrue(new File(artifactBaseDir, "1.0/large-artifact-2GB-1.0.jar.sha1").exists());
        assertTrue(new File(artifactBaseDir, "1.0/large-artifact-2GB-1.0.pom").exists());
        assertTrue(new File(artifactBaseDir, "1.0/large-artifact-2GB-1.0.pom.md5").exists());
        assertTrue(new File(artifactBaseDir, "1.0/large-artifact-2GB-1.0.pom.sha1").exists());
        assertTrue(new File(artifactBaseDir, "maven-metadata.xml").exists());
        assertTrue(new File(artifactBaseDir, "maven-metadata.xml.md5").exists());
        assertTrue(new File(artifactBaseDir, "maven-metadata.xml.sha1").exists());

        System.out.println(artifactBaseDir.getAbsolutePath());
    }

}
