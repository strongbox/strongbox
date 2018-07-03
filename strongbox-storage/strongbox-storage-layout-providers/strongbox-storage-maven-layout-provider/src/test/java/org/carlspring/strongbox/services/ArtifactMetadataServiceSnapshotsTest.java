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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author stodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
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

    private static MavenArtifact artifact;

    private static MavenArtifact pluginArtifact;

    private static MavenArtifact mergeArtifact;

    @Inject
    private ArtifactMetadataService artifactMetadataService;

    private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd.HHmmss");

    private Calendar calendar = Calendar.getInstance();

    @Before
    public void initialize()
            throws Exception
    {
        createRepository(STORAGE0, REPOSITORY_SNAPSHOTS, RepositoryPolicyEnum.SNAPSHOT.getPolicy(), false);

        // Create snapshot artifacts
        artifact = createTimestampedSnapshotArtifact(REPOSITORY_BASEDIR.getAbsolutePath(),
                                                     "org.carlspring.strongbox",
                                                     "strongbox-metadata",
                                                     "2.0",
                                                     "jar",
                                                     CLASSIFIERS,
                                                     5);

        changeCreationDate(artifact);

        // Create a snapshot without a timestamp
        // artifactNoTimestamp = createSnapshot(REPOSITORY_BASEDIR.getAbsolutePath(),
        // "org.carlspring.strongbox:strongbox-metadata-without-timestamp:2.0-SNAPSHOT:jar");

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
    }

    @Test
    public void testSnapshotMetadataRebuild()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException
    {
        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, ARTIFACT_BASE_PATH_STRONGBOX_METADATA);

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                REPOSITORY_SNAPSHOTS,
                                                                "org/carlspring/strongbox/strongbox-metadata");

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals("Incorrect artifactId!", artifact.getArtifactId(), metadata.getArtifactId());
        assertEquals("Incorrect groupId!", artifact.getGroupId(), metadata.getGroupId());

        assertNotNull("No versioning information could be found in the metadata!", versioning.getVersions().size());
        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions().size());
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
        assertTrue("Unexpected set of versions!", MetadataHelper.containsVersion(metadataBefore, "1.0-SNAPSHOT"));

        artifactMetadataService.removeVersion(STORAGE0,
                                              REPOSITORY_SNAPSHOTS,
                                              artifactPath,
                                              "1.0-SNAPSHOT",
                                              MetadataType.ARTIFACT_ROOT_LEVEL);

        Metadata metadataAfter = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        assertNotNull(metadataAfter);
        assertFalse("Unexpected set of versions!", MetadataHelper.containsVersion(metadataAfter, "1.0-SNAPSHOT"));
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
        assertTrue("Failed to add timestamped SNAPSHOT version to metadata!",
                   MetadataHelper.containsTimestampedSnapshotVersion(metadataAfter, addedArtifact.getVersion()));
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

        MavenArtifact snapshotArtifact = new MavenRepositoryArtifact("org.carlspring.strongbox.snapshots", "metadata", version);

        artifactMetadataService.rebuildMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);
        Metadata snapshotMetadata = artifactMetadataService.getMetadata(STORAGE0, REPOSITORY_SNAPSHOTS, artifactPath);

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();
        Versioning snapshotVersioning = snapshotMetadata.getVersioning();

        assertEquals("Incorrect artifactId!", snapshotArtifact.getArtifactId(), metadata.getArtifactId());
        assertEquals("Incorrect groupId!", snapshotArtifact.getGroupId(), metadata.getGroupId());

        assertNotNull("No versioning information could be found in the metadata!", versioning.getVersions().size());
        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions().size());
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
        artifactMetadataService.rebuildMetadata(STORAGE0,
                                                REPOSITORY_SNAPSHOTS,
                                                "org/carlspring/strongbox/maven/strongbox-metadata-plugin");

        Metadata metadata = artifactMetadataService.getMetadata(STORAGE0,
                                                                REPOSITORY_SNAPSHOTS,
                                                                "org/carlspring/strongbox/maven/strongbox-metadata-plugin");

        assertNotNull(metadata);

        Versioning versioning = metadata.getVersioning();

        assertEquals("Incorrect artifactId!", pluginArtifact.getArtifactId(), metadata.getArtifactId());
        assertEquals("Incorrect groupId!", pluginArtifact.getGroupId(), metadata.getGroupId());
        assertNull("Incorrect latest release version!", versioning.getRelease());

        assertEquals("Incorrect number of versions stored in metadata!", 1, versioning.getVersions().size());
    }

    @Test
    public void testMetadataMerge()
            throws IOException, XmlPullParserException, NoSuchAlgorithmException, ProviderImplementationException
    {
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

        assertEquals("Incorrect latest release version!", "1.3-SNAPSHOT", metadata.getVersioning().getLatest());
        assertEquals("Incorrect number of versions stored in metadata!", 3,
                     metadata.getVersioning().getVersions().size());
    }

}
