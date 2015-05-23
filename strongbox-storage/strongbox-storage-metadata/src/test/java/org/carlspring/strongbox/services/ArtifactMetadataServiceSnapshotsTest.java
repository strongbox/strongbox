package org.carlspring.strongbox.services;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.carlspring.maven.commons.DetachedArtifact;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author stodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class ArtifactMetadataServiceSnapshotsTest
        extends TestCaseWithArtifactGeneration
{

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/snapshots");

    public static final String[] CLASSIFIERS = { "javadoc", "sources", "source-release" };

    public static final String ARTIFACT_BASE_PATH_STRONGBOX_METADATA = "org/carlspring/strongbox/strongbox-metadata";

    private static Artifact artifact;

    private static Artifact pluginArtifact;

    private static Artifact mergeArtifact;

    private static Artifact artifactNoTimestamp;

    @Autowired
    private ArtifactMetadataService artifactMetadataService;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");

    private Calendar calendar = Calendar.getInstance();

    private static boolean initialized;


    @Before
    public void setUp()
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        if (!initialized)
        {
            //noinspection ResultOfMethodCallIgnored
            REPOSITORY_BASEDIR.mkdirs();

            // Create snapshot artifacts
            artifact = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                                                         "org.carlspring.strongbox",
                                                         "strongbox-metadata",
                                                         "2.0",
                                                         "jar",
                                                         CLASSIFIERS,
                                                         3);

            changeCreationDate(artifact);

            // Create a snapshot without a timestamp
            artifactNoTimestamp = createSnapshot(REPOSITORY_BASEDIR.getAbsolutePath(), "org.carlspring.strongbox:strongbox-metadata-without-timestamp:2.0-SNAPSHOT:jar");

            // Create an artifact for metadata merging tests
            mergeArtifact = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                                                              "org.carlspring.strongbox",
                                                              "strongbox-metadata-merge",
                                                              "2.0",
                                                              CLASSIFIERS);

            // Create plugin artifact
            pluginArtifact = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                                                               "org.carlspring.strongbox.maven",
                                                               "strongbox-metadata-plugin",
                                                               "1.1",
                                                               "maven-plugin",
                                                               null);

            initialized = true;
        }
    }

    @Test
    public void testSnapshotMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        artifactMetadataService.rebuildMetadata("storage0", "snapshots", ARTIFACT_BASE_PATH_STRONGBOX_METADATA);

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "snapshots", "org/carlspring/strongbox/strongbox-metadata");

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals("Incorrect artifactId!", artifact.getArtifactId(), metadata.getArtifactId());
        assertEquals("Incorrect groupId!", artifact.getGroupId(), metadata.getGroupId());
        //assertEquals("Incorrect latest release version!", artifact.getVersion(), versioning.getRelease());

        assertNotNull("No versioning information could be found in the metadata!",
                      versioning.getVersions().size());
        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions().size());
    }

    @Test
    public void testSnapshotWithoutTimestampMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        String version = "2.0-SNAPSHOT";

        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(), "org.carlspring.strongbox.snapshots:metadata:" + version);
        final String artifactPath = "org/carlspring/strongbox/snapshots/metadata";

        Artifact snapshotArtifact = new DetachedArtifact("org.carlspring.strongbox.snapshots", "metadata", version);

        artifactMetadataService.rebuildMetadata("storage0", "snapshots", artifactPath);

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "snapshots", artifactPath);
        Metadata snapshotMetadata = artifactMetadataService.getMetadata("storage0", "snapshots", artifactPath);

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();
        Versioning snapshotVersioning = snapshotMetadata.getVersioning();

        assertEquals("Incorrect artifactId!", snapshotArtifact.getArtifactId(), metadata.getArtifactId());
        assertEquals("Incorrect groupId!", snapshotArtifact.getGroupId(), metadata.getGroupId());
        //assertEquals("Incorrect latest release version!", artifact.getVersion(), versioning.getRelease());

        assertNotNull("No versioning information could be found in the metadata!", versioning.getVersions().size());
        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions().size());
        // assertEquals(version, metadata.getVersion());
        assertEquals(version, versioning.getLatest());
        assertNotNull("Failed to set lastUpdated field!", versioning.getLastUpdated());

        assertNotNull("No versioning information could be found in the metadata!",
                      snapshotVersioning.getVersions().size());
        assertEquals("Incorrect number of versions stored in metadata!", 1, snapshotVersioning.getVersions().size());
        assertEquals(version, snapshotVersioning.getLatest());
        assertNotNull("Failed to set lastUpdated field!", snapshotVersioning.getLastUpdated());

    }

    @Test
    public void testSnapshotPluginMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        artifactMetadataService.rebuildMetadata("storage0",
                                                "snapshots",
                                                "/org/carlspring/strongbox/maven/strongbox-metadata-plugin");

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "snapshots", "org/carlspring/strongbox/maven/strongbox-metadata-plugin");

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals("Incorrect artifactId!", pluginArtifact.getArtifactId(), metadata.getArtifactId());
        assertEquals("Incorrect groupId!", pluginArtifact.getGroupId(), metadata.getGroupId());
        assertNull("Incorrect latest release version!", versioning.getRelease());

        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions().size());
    }

    @Test
    public void testMetadataMerge()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        // Generate a proper maven-metadata.xml
        artifactMetadataService.rebuildMetadata("storage0", "snapshots", "org/carlspring/strongbox/strongbox-metadata-merge");

        // Generate metadata to merge
        Metadata mergeMetadata = new Metadata();
        Versioning appendVersioning = new Versioning();

        appendVersioning.addVersion("1.0-SNAPSHOT");
        appendVersioning.addVersion("1.3-SNAPSHOT");

        Snapshot snapshot = new Snapshot();
        snapshot.setTimestamp(formatter.format(calendar.getTime()));
        snapshot.setBuildNumber(1);

        appendVersioning.setRelease(null);
        appendVersioning.setSnapshot(snapshot);
        appendVersioning.setLatest("1.3-SNAPSHOT");

        mergeMetadata.setVersioning(appendVersioning);

        // Merge
        artifactMetadataService.mergeMetadata("storage0", "snapshots", mergeArtifact, mergeMetadata);

        Metadata metadata = artifactMetadataService.getMetadata("storage0", "snapshots", "org/carlspring/strongbox/strongbox-metadata-merge");

        assertNotNull(metadata);

        assertEquals("Incorrect latest release version!", "1.3-SNAPSHOT", metadata.getVersioning().getLatest());
        // assertEquals("Incorrect latest release version!", null, metadata.getVersioning().getRelease());
        assertEquals("Incorrect number of versions stored in metadata!",
                     3,
                     metadata.getVersioning().getVersions().size());
    }

}
