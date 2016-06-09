package org.carlspring.strongbox.storage.metadata;

import org.carlspring.maven.commons.util.ArtifactUtils;

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

public class MetadataMerger
{

    public Metadata updateMetadataAtVersionLevel(Artifact artifact, Metadata metadata)
    {
        if (metadata == null)
        {
            metadata = new Metadata();
            metadata.setGroupId(artifact.getGroupId());
            metadata.setArtifactId(artifact.getArtifactId());
            String newVersion = artifact.getVersion().substring(0, artifact.getVersion().indexOf("-") +1).concat("SNAPSHOT");
            metadata.setVersion(newVersion);
        }
        // I generate timestamp once for all the merging
        String timestamp = MetadataHelper.LAST_UPDATED_FIELD_FORMATTER.format(Calendar.getInstance().getTime());

        // Update metadata o fill it for first time in case I have just created it
        Versioning versioning = metadata.getVersioning();
        if (versioning == null)
        {
            versioning = new Versioning();
            metadata.setVersioning(versioning);
        }

        Snapshot snapshot = versioning.getSnapshot();
        if (snapshot == null)
        {
            snapshot = new Snapshot();
            versioning.setSnapshot(snapshot);
        }
        snapshot.setBuildNumber(snapshot.getBuildNumber() + 1);
        snapshot.setTimestamp(timestamp.substring(0, 7) + "." + timestamp.substring(8));

        versioning.setLastUpdated(timestamp);

        List<SnapshotVersion> snapshotVersions = versioning.getSnapshotVersions();
        for (SnapshotVersion snapshotVersion : snapshotVersions)
        {
            snapshotVersion.setUpdated(timestamp);
        }

        snapshotVersions.addAll(createNewSnapshotVersions(artifact.getVersion(), timestamp, snapshot.getBuildNumber()));
        return metadata;
    }

    public Metadata updateMetadataAtArtifactLevel(Artifact artifact, Metadata metadata)
    {
        if (metadata == null)
        {
            metadata = new Metadata();
            metadata.setGroupId(artifact.getGroupId());
            metadata.setArtifactId(artifact.getArtifactId());
        }
        String newVersion = ArtifactUtils.isReleaseVersion(artifact.getVersion())? artifact.getVersion() : artifact.getVersion().substring(0, artifact.getVersion().indexOf("-") +1).concat("SNAPSHOT");
        Versioning versioning = metadata.getVersioning();
        if (versioning == null)
        {
            versioning = new Versioning();
            metadata.setVersioning(versioning);
        }
        versioning.setLatest(newVersion);
        if (ArtifactUtils.isReleaseVersion(artifact.getVersion()))
        {
            versioning.setRelease(newVersion);
        }
        List<String> versions = versioning.getVersions();
        
        if (!versions.contains(newVersion))
        {
            versions.add(newVersion);
        }
        versioning.setLastUpdated(MetadataHelper.LAST_UPDATED_FIELD_FORMATTER.format(Calendar.getInstance().getTime()));
        return metadata;
    }

    public Metadata updateMetadataAtGroupLevel(PluginArtifact artifact, Metadata metadata)
    {
        if (metadata == null)
        {
            metadata = new Metadata();
        }
        List<Plugin> plugins = metadata.getPlugins();
        boolean found  = false;
        for (Plugin plugin : plugins)
        {
            if (plugin.getArtifactId().equals(artifact.getArtifactId()))
            {
                found = true;
                break;
            }
        }
        if (!found)
        {
            Plugin plugin = new Plugin();
            // TODO: Figure out how to get plugin name from artifact
            plugin.setName("");
            plugin.setArtifactId(artifact.getArtifactId());
            // TODO: Figure out how to get plugin prefix from artifact
            plugin.setPrefix("");

            plugins.add(plugin);
        }
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
