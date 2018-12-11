package org.carlspring.strongbox.services;

import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenRepositoryArtifact;
import org.carlspring.strongbox.config.Maven2LayoutProviderTestConfig;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.carlspring.strongbox.storage.metadata.MetadataType;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author stodorov
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
@ContextConfiguration(classes = Maven2LayoutProviderTestConfig.class)
public class ArtifactMetadataServiceSnapshotsTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    private static final String REPOSITORY_SNAPSHOTS = "amss-snapshots";

    private static final File REPOSITORY_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                            "/storages/" + STORAGE0 + "/" + REPOSITORY_SNAPSHOTS);

    private static final String[] CLASSIFIERS = { "javadoc",
                                                  "sources",
                                                  "source-release" };

    private static final String ARTIFACT_BASE_PATH_STRONGBOX_METADATA = "org/carlspring/strongbox/strongbox-metadata";


    @Inject
    private ArtifactMetadataService artifactMetadataService;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");

    private Calendar calendar = Calendar.getInstance();


    @BeforeEach
    public void initialize()
            throws Exception
    {
        createRepository(STORAGE0, REPOSITORY_SNAPSHOTS, RepositoryPolicyEnum.SNAPSHOT.getPolicy(), false);
    }

    @Test
    public void testSnapshotMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        // Create snapshot artifacts
        MavenArtifact artifact = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                                                                   "org.carlspring.strongbox",
                                                                   "strongbox-metadata",
                                                                   "2.0",
                                                                   "jar",
                                                                   CLASSIFIERS,
                                                                   5);

        changeCreationDate(artifact);

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, ARTIFACT_BASE_PATH_STRONGBOX_METADATA);

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                REPOSITORY_SNAPSHOTS,
                                                                "org/carlspring/strongbox/strongbox-metadata");

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals(artifact.getArtifactId(), metadata.getArtifactId(), "Incorrect artifactId!");
        assertEquals(artifact.getGroupId(), metadata.getGroupId(), "Incorrect groupId!");

        assertNotNull(versioning.getVersions().size(), "No versioning information could be found in the metadata!");
        assertEquals(1, versioning.getVersions().size(), "Incorrect number of versions stored in metadata!");
    }

    @Test
    public void testDeleteVersionFromMetadata()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        // Create snapshot artifacts
        MavenArtifact deletedArtifact = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                                                                          "org.carlspring.strongbox",
                                                                          "deleted",
                                                                          "1.0",
                                                                          "jar",
                                                                          CLASSIFIERS,
                                                                          5);

        changeCreationDate(deletedArtifact);

        String artifactPath = "org/carlspring/strongbox/deleted";

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        Metadata metadataBefore = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        assertNotNull(metadataBefore);
        assertTrue(MetadataHelper.containsVersion(metadataBefore, "1.0-SNAPSHOT"), "Unexpected set of versions!");

        artifactMetadataService.removeVersion(STORAGE0,
                                              REPOSITORY_SNAPSHOTS,
                                              artifactPath,
                                              "1.0-SNAPSHOT",
                                              MetadataType.ARTIFACT_ROOT_LEVEL);

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        assertNotNull(metadataAfter);
        assertFalse(MetadataHelper.containsVersion(metadataAfter, "1.0-SNAPSHOT"), "Unexpected set of versions!");
    }

    @Test
    public void testAddTimestampedSnapshotVersionToMetadata()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        // Create snapshot artifacts
        MavenArtifact addedArtifact = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                                                                        "org.carlspring.strongbox",
                                                                        "added",
                                                                        "2.0",
                                                                        "jar",
                                                                        CLASSIFIERS,
                                                                        5);

        changeCreationDate(addedArtifact);

        String artifactPath = "org/carlspring/strongbox/added";

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        String metadataPath = artifactPath + "/2.0-SNAPSHOT";

        Metadata metadataBefore = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, metadataPath);

        assertTrue(MetadataHelper.containsTimestampedSnapshotVersion(metadataBefore, addedArtifact.getVersion(), null));

        String timestamp = formatter.format(calendar.getTime());
        String version = "2.0-" + timestamp + "-" + 6;

        addedArtifact = new MavenRepositoryArtifact(addedArtifact.getGroupId(),
                                                    addedArtifact.getArtifactId(),
                                                    version);

        artifactMetadataService.addTimestampedSnapshotVersion(STORAGE0,
                                                              REPOSITORY_SNAPSHOTS,
                                                              artifactPath,
                                                              addedArtifact.getVersion(),
                                                              null,
                                                              "jar");

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, metadataPath);

        assertNotNull(metadataAfter);
        assertTrue(MetadataHelper.containsTimestampedSnapshotVersion(metadataAfter, addedArtifact.getVersion()),
                   "Failed to add timestamped SNAPSHOT version to metadata!");
    }

    @Test
    public void testDeleteTimestampedSnapshotVersionFromMetadata()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        // Create snapshot artifacts
        MavenArtifact deletedArtifact = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                                                                          "org.carlspring.strongbox",
                                                                          "deleted",
                                                                          "2.0",
                                                                          "jar",
                                                                          CLASSIFIERS,
                                                                          5);

        changeCreationDate(deletedArtifact);

        String artifactPath = "org/carlspring/strongbox/deleted";

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        String metadataPath = artifactPath + "/2.0-SNAPSHOT";

        Metadata metadataBefore = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, metadataPath);

        assertTrue(
                MetadataHelper.containsTimestampedSnapshotVersion(metadataBefore, deletedArtifact.getVersion(), null));

        artifactMetadataService.removeTimestampedSnapshotVersion(STORAGE0,
                                                                 REPOSITORY_SNAPSHOTS,
                                                                 artifactPath,
                                                                 deletedArtifact.getVersion(),
                                                                 null);

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, metadataPath);

        assertNotNull(metadataAfter);
        assertFalse(MetadataHelper.containsTimestampedSnapshotVersion(metadataAfter, deletedArtifact.getVersion()));
    }

    @Test
    public void testSnapshotWithoutTimestampMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        String version = "2.0-SNAPSHOT";

        generateArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                         "org.carlspring.strongbox.snapshots:metadata:" + version);
        final String artifactPath = "org/carlspring/strongbox/snapshots/metadata";

        MavenArtifact snapshotArtifact = new MavenRepositoryArtifact("org.carlspring.strongbox.snapshots", "metadata",
                                                                     version);

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);
        Metadata snapshotMetadata = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();
        Versioning snapshotVersioning = snapshotMetadata.getVersioning();

        assertEquals(snapshotArtifact.getArtifactId(), metadata.getArtifactId(), "Incorrect artifactId!");
        assertEquals(snapshotArtifact.getGroupId(), metadata.getGroupId(), "Incorrect groupId!");

        assertNotNull(versioning.getVersions(), "No versioning information could be found in the metadata!");
        assertEquals(1, versioning.getVersions().size(), "Incorrect number of versions stored in metadata!");
        assertEquals(version, versioning.getLatest());
        assertNotNull(versioning.getLastUpdated(), "Failed to set lastUpdated field!");

        assertNotNull(snapshotVersioning.getVersions(),
                      "No versioning information could be found in the metadata!");
        assertEquals(1, snapshotVersioning.getVersions().size(), "Incorrect number of versions stored in metadata!");
        assertEquals(version, snapshotVersioning.getLatest());
        assertNotNull(snapshotVersioning.getLastUpdated(), "Failed to set lastUpdated field!");
    }

    @Test
    public void testSnapshotPluginMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        // Create plugin artifact
        MavenArtifact pluginArtifact = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                                                                         "org.carlspring.strongbox.maven",
                                                                         "strongbox-metadata-plugin",
                                                                         "1.1",
                                                                         "maven-plugin",
                                                                         null);

        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                REPOSITORY_SNAPSHOTS,
                                                "org/carlspring/strongbox/maven/strongbox-metadata-plugin");

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                REPOSITORY_SNAPSHOTS,
                                                                "org/carlspring/strongbox/maven/strongbox-metadata-plugin");

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals(pluginArtifact.getArtifactId(), metadata.getArtifactId(), "Incorrect artifactId!");
        assertEquals(pluginArtifact.getGroupId(), metadata.getGroupId(), "Incorrect groupId!");
        assertNull(versioning.getRelease(), "Incorrect latest release version!");

        assertEquals(1, versioning.getVersions().size(), "Incorrect number of versions stored in metadata!");
    }

    @Test
    public void testMetadataMerge()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException, ProviderImplementationException
    {
        // Create an artifact for metadata merging tests
        MavenArtifact mergeArtifact = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                                                                        "org.carlspring.strongbox",
                                                                        "strongbox-metadata-merge",
                                                                        "2.0",
                                                                        CLASSIFIERS);

        // Generate a proper maven-metadata.xml
        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                REPOSITORY_SNAPSHOTS,
                                                "org/carlspring/strongbox/strongbox-metadata-merge");

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
        artifactMetadataService.mergeMetadata(mergeArtifact, mergeMetadata);

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                REPOSITORY_SNAPSHOTS,
                                                                "org/carlspring/strongbox/strongbox-metadata-merge");

        assertNotNull(metadata);

        assertEquals("1.3-SNAPSHOT", metadata.getVersioning().getLatest(), "Incorrect latest release version!");
        assertEquals(3, metadata.getVersioning().getVersions().size(),
                     "Incorrect number of versions stored in metadata!");
    }

}
