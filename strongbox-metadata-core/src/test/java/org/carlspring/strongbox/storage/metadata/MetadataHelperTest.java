package org.carlspring.strongbox.storage.metadata;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Plugin;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.project.artifact.PluginArtifact;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MetadataHelperTest
{

    private static final Object POM = "pom";

    private static final String JAR = "jar";

    private static final String VERSION = "1.1";

    private static final String PRE_VERSION = "1.0";

    private static final String JAVADOC = "javadoc";

    private static final String ANOTHER_ARTIFACT_ID = "mockito-all";

    private static final String ARTIFACT_ID = "strongbox-metadata-core";

    private static final String SNAPSHOT_VERSION = "1.0-SNAPSHOT";

    private static final String GROUP_ID = "org.carlspring.strongbox";

    private MetadataMerger metadataMerger;

    @Mock
    private Artifact artifact;

    @Mock
    private PluginArtifact pluginArtifact;

    @Before
    public void setUp()
    {
        initMocks(this);
        metadataMerger = new MetadataMerger();
    }

    @Test
    public void versionLevelCreateNewMetadaTest()
    {

        // Given
        when(artifact.getGroupId()).thenReturn(GROUP_ID);
        when(artifact.getArtifactId()).thenReturn(ARTIFACT_ID);
        when(artifact.getVersion()).thenReturn(SNAPSHOT_VERSION);

        // When
        Metadata metadata = metadataMerger.updateMetadataAtVersionLevel(artifact, null);

        // Then
        Assert.assertEquals(GROUP_ID, metadata.getGroupId());
        Assert.assertEquals(ARTIFACT_ID, metadata.getArtifactId());
        Assert.assertEquals(SNAPSHOT_VERSION, metadata.getVersion());

        Assert.assertNotNull(metadata.getVersioning());
        Assert.assertNotNull(metadata.getVersioning().getSnapshot());
        Assert.assertEquals(1, metadata.getVersioning().getSnapshot().getBuildNumber());
        Assert.assertNotNull(metadata.getVersioning().getSnapshot().getTimestamp());
        Assert.assertNotNull(metadata.getVersioning().getLastUpdated());
        Assert.assertEquals(3, metadata.getVersioning().getSnapshotVersions().size());

        Assert.assertEquals(JAVADOC, metadata.getVersioning().getSnapshotVersions().get(0).getClassifier());
        Assert.assertEquals(JAR, metadata.getVersioning().getSnapshotVersions().get(0).getExtension());
        Assert.assertNotNull(metadata.getVersioning().getSnapshotVersions().get(0).getUpdated());

        Assert.assertEquals(JAR, metadata.getVersioning().getSnapshotVersions().get(1).getExtension());
        Assert.assertNotNull(metadata.getVersioning().getSnapshotVersions().get(1).getUpdated());

        Assert.assertEquals(POM, metadata.getVersioning().getSnapshotVersions().get(2).getExtension());
        Assert.assertNotNull(metadata.getVersioning().getSnapshotVersions().get(2).getUpdated());

        Assert.assertTrue(metadata.getVersioning().getSnapshotVersions().get(0).getVersion()
                .equals(metadata.getVersioning().getSnapshotVersions().get(1).getVersion()));
        Assert.assertTrue(metadata.getVersioning().getSnapshotVersions().get(1).getVersion()
                .equals(metadata.getVersioning().getSnapshotVersions().get(2).getVersion()));
    }

    @Test
    public void versionLevelUpdateExistingMetadataTest()
    {
        // Given
        Metadata metadata = createVersionLevelMetadata();
        when(artifact.getVersion()).thenReturn(SNAPSHOT_VERSION);

        // When
        metadata = metadataMerger.updateMetadataAtVersionLevel(artifact, metadata);

        // Then
        Assert.assertEquals(GROUP_ID, metadata.getGroupId());
        Assert.assertEquals(ARTIFACT_ID, metadata.getArtifactId());
        Assert.assertEquals(SNAPSHOT_VERSION, metadata.getVersion());

        Assert.assertNotNull(metadata.getVersioning());
        Assert.assertNotNull(metadata.getVersioning().getSnapshot());
        Assert.assertEquals(2, metadata.getVersioning().getSnapshot().getBuildNumber());
        Assert.assertNotNull(metadata.getVersioning().getSnapshot().getTimestamp());
        Assert.assertNotNull(metadata.getVersioning().getLastUpdated());
        Assert.assertEquals(6, metadata.getVersioning().getSnapshotVersions().size());

        Assert.assertEquals(JAVADOC, metadata.getVersioning().getSnapshotVersions().get(3).getClassifier());
        Assert.assertEquals(JAR, metadata.getVersioning().getSnapshotVersions().get(3).getExtension());
        Assert.assertNotNull(metadata.getVersioning().getSnapshotVersions().get(3).getUpdated());

        Assert.assertEquals(JAR, metadata.getVersioning().getSnapshotVersions().get(4).getExtension());
        Assert.assertNotNull(metadata.getVersioning().getSnapshotVersions().get(4).getUpdated());

        Assert.assertEquals(POM, metadata.getVersioning().getSnapshotVersions().get(5).getExtension());
        Assert.assertNotNull(metadata.getVersioning().getSnapshotVersions().get(5).getUpdated());

        Assert.assertTrue(metadata.getVersioning().getSnapshotVersions().get(3).getVersion()
                .equals(metadata.getVersioning().getSnapshotVersions().get(4).getVersion()));
        Assert.assertTrue(metadata.getVersioning().getSnapshotVersions().get(4).getVersion()
                .equals(metadata.getVersioning().getSnapshotVersions().get(5).getVersion()));
    }

    @Test
    public void artifactLevelCreateNewMetadataTest()
    {
        // Given
        when(artifact.getGroupId()).thenReturn(GROUP_ID);
        when(artifact.getArtifactId()).thenReturn(ARTIFACT_ID);
        when(artifact.getVersion()).thenReturn(VERSION);

        // When
        Metadata metadata = metadataMerger.updateMetadataAtArtifactLevel(artifact, null);

        // Then
        Assert.assertEquals(GROUP_ID, metadata.getGroupId());
        Assert.assertEquals(ARTIFACT_ID, metadata.getArtifactId());

        Assert.assertNotNull(metadata.getVersioning());
        Assert.assertEquals(VERSION, metadata.getVersioning().getLatest());
        Assert.assertEquals(VERSION, metadata.getVersioning().getRelease());
        Assert.assertNotNull(metadata.getVersioning().getLastUpdated());

        Assert.assertEquals(1, metadata.getVersioning().getVersions().size());
        Assert.assertEquals(VERSION, metadata.getVersioning().getVersions().get(0));
    }

    @Test
    public void artifactLevelUpdateMetadataTest()
    {
        // Given
        Metadata metadata = createArtifactLevelMetadata();
        when(artifact.getVersion()).thenReturn(VERSION);

        // When
        metadata = metadataMerger.updateMetadataAtArtifactLevel(artifact, metadata);

        Assert.assertEquals(GROUP_ID, metadata.getGroupId());
        Assert.assertEquals(ARTIFACT_ID, metadata.getArtifactId());
        Assert.assertEquals(VERSION, metadata.getVersioning().getLatest());
        Assert.assertEquals(VERSION, metadata.getVersioning().getRelease());
        Assert.assertEquals(2, metadata.getVersioning().getVersions().size());
        Assert.assertTrue(metadata.getVersioning().getVersions().contains(VERSION));
        Assert.assertTrue(metadata.getVersioning().getVersions().contains(PRE_VERSION));
        Assert.assertNotNull(metadata.getVersioning().getLastUpdated());
    }

    @Test
    public void artifactLevelUpdateMetadataVersionExistsTest()
    {
        // Given
        Metadata metadata = createArtifactLevelMetadata();
        when(artifact.getVersion()).thenReturn(PRE_VERSION);

        // When
        metadata = metadataMerger.updateMetadataAtArtifactLevel(artifact, metadata);

        // Then
        Assert.assertEquals(GROUP_ID, metadata.getGroupId());
        Assert.assertEquals(ARTIFACT_ID, metadata.getArtifactId());
        Assert.assertEquals(PRE_VERSION, metadata.getVersioning().getLatest());
        Assert.assertEquals(PRE_VERSION, metadata.getVersioning().getRelease());
        Assert.assertEquals(1, metadata.getVersioning().getVersions().size());
        Assert.assertTrue(metadata.getVersioning().getVersions().contains(PRE_VERSION));
        Assert.assertNotNull(metadata.getVersioning().getLastUpdated());

    }

    @Test
    public void artifactLevelUpdateMetadataSnapshotTest()
    {
        // Given
        Metadata metadata = createArtifactLevelMetadata();
        when(artifact.getVersion()).thenReturn(SNAPSHOT_VERSION);

        // When
        metadata = metadataMerger.updateMetadataAtArtifactLevel(artifact, metadata);

        Assert.assertEquals(GROUP_ID, metadata.getGroupId());
        Assert.assertEquals(ARTIFACT_ID, metadata.getArtifactId());
        Assert.assertEquals(SNAPSHOT_VERSION, metadata.getVersioning().getLatest());
        Assert.assertEquals(PRE_VERSION, metadata.getVersioning().getRelease());
        Assert.assertEquals(2, metadata.getVersioning().getVersions().size());
        Assert.assertTrue(metadata.getVersioning().getVersions().contains(SNAPSHOT_VERSION));
        Assert.assertTrue(metadata.getVersioning().getVersions().contains(PRE_VERSION));
        Assert.assertNotNull(metadata.getVersioning().getLastUpdated());
    }

    @Test
    public void groupLevelCreateMetadataTest()
    {
        // Given
        when(pluginArtifact.getGroupId()).thenReturn(GROUP_ID);
        when(pluginArtifact.getArtifactId()).thenReturn(ARTIFACT_ID);

        // When
        Metadata metadata = metadataMerger.updateMetadataAtGroupLevel((PluginArtifact) pluginArtifact, null);

        // Then
        Assert.assertNotNull(metadata.getPlugins());
        Assert.assertEquals(1, metadata.getPlugins().size());
        Assert.assertEquals(ARTIFACT_ID, metadata.getPlugins().get(0).getArtifactId());
        Assert.assertNotNull(metadata.getPlugins().get(0).getName());
        Assert.assertNotNull(metadata.getPlugins().get(0).getPrefix());
    }

    @Test
    public void groupLevelUpdateMetadataAddPluginTest()
    {
        // Given
        Metadata metadata = createGroupLevelMetadata();
        when(pluginArtifact.getArtifactId()).thenReturn(ARTIFACT_ID);

        // When
        metadata = metadataMerger.updateMetadataAtGroupLevel(pluginArtifact, metadata);

        // Then
        Assert.assertEquals(2, metadata.getPlugins().size());
        Assert.assertEquals(ARTIFACT_ID, metadata.getPlugins().get(1).getArtifactId());
        Assert.assertEquals("", metadata.getPlugins().get(1).getName());
        Assert.assertEquals("", metadata.getPlugins().get(1).getPrefix());
    }

    @Test
    public void groupLevelUpdateMetadataPluginExistsTest()
    {
        // Given
        Metadata metadata = createGroupLevelMetadata();
        when(pluginArtifact.getArtifactId()).thenReturn(ANOTHER_ARTIFACT_ID);

        // When
        metadata = metadataMerger.updateMetadataAtGroupLevel(pluginArtifact, metadata);

        // Then
        Assert.assertEquals(1, metadata.getPlugins().size());
        Assert.assertEquals(ANOTHER_ARTIFACT_ID, metadata.getPlugins().get(0).getArtifactId());
        Assert.assertEquals("", metadata.getPlugins().get(0).getName());
        Assert.assertEquals("", metadata.getPlugins().get(0).getPrefix());
    }

    private Metadata createGroupLevelMetadata()
    {
        Metadata metadata = new Metadata();

        Plugin plugin = new Plugin();
        plugin.setArtifactId(ANOTHER_ARTIFACT_ID);
        plugin.setName("");
        plugin.setPrefix("");

        List<Plugin> plugins = new ArrayList<Plugin>();
        plugins.add(plugin);
        metadata.setPlugins(plugins);
        return metadata;
    }

    private Metadata createArtifactLevelMetadata()
    {
        Metadata metadata = new Metadata();

        metadata.setGroupId(GROUP_ID);
        metadata.setArtifactId(ARTIFACT_ID);
        metadata.setVersioning(new Versioning());
        metadata.getVersioning().setLatest(PRE_VERSION);
        metadata.getVersioning().setRelease(PRE_VERSION);
        List<String> versions = new ArrayList<String>();
        versions.add(PRE_VERSION);
        metadata.getVersioning().setVersions(versions);
        metadata.getVersioning()
                .setLastUpdated(MetadataHelper.LAST_UPDATED_FIELD_FORMATTER.format(Calendar.getInstance().getTime()));
        return metadata;
    }

    private Metadata createVersionLevelMetadata()
    {
        Metadata metadata = new Metadata();
        metadata.setGroupId(GROUP_ID);
        metadata.setArtifactId(ARTIFACT_ID);
        metadata.setVersion(SNAPSHOT_VERSION);

        String timestamp = MetadataHelper.LAST_UPDATED_FIELD_FORMATTER.format(Calendar.getInstance().getTime());

        Snapshot snapshot = new Snapshot();
        snapshot.setBuildNumber(1);
        snapshot.setTimestamp(timestamp.substring(0, 7) + "." + timestamp.substring(8));

        List<SnapshotVersion> snapshotVersions = new ArrayList<SnapshotVersion>();
        snapshotVersions.addAll(createNewSnapshotVersions(SNAPSHOT_VERSION, timestamp, 1));

        Versioning versioning = new Versioning();
        versioning.setSnapshot(snapshot);
        versioning.setLastUpdated(timestamp);
        versioning.setSnapshotVersions(snapshotVersions);

        metadata.setVersioning(versioning);
        return metadata;
    }

    private Collection<SnapshotVersion> createNewSnapshotVersions(String version, String timestamp, int buildNumber)
    {
        Collection<SnapshotVersion> toReturn = new ArrayList<SnapshotVersion>();

        SnapshotVersion sv1 = new SnapshotVersion();
        SnapshotVersion sv2 = new SnapshotVersion();
        SnapshotVersion sv3 = new SnapshotVersion();

        toReturn.add(sv1);
        toReturn.add(sv2);
        toReturn.add(sv3);

        sv1.setClassifier("javadoc");
        sv1.setExtension("jar");
        sv1.setVersion(version.replace("SNAPSHOT",
                timestamp.substring(0, 7) + "." + timestamp.substring(8) + "-" + buildNumber));
        sv1.setUpdated(timestamp);

        sv2.setExtension("jar");
        sv2.setVersion(version.replace("SNAPSHOT",
                timestamp.substring(0, 7) + "." + timestamp.substring(8) + "-" + buildNumber));
        sv2.setUpdated(timestamp);

        sv3.setExtension("pom");
        sv3.setVersion(version.replace("SNAPSHOT",
                timestamp.substring(0, 7) + "." + timestamp.substring(8) + "-" + buildNumber));
        sv3.setUpdated(timestamp);

        return toReturn;
    }
}
