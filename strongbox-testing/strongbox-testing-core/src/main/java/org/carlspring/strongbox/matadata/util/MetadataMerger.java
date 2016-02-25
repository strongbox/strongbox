package org.carlspring.strongbox.matadata.util;

import java.io.IOException;
import java.io.InputStream;
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
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.project.artifact.PluginArtifact;
import org.carlspring.strongbox.client.ArtifactClient;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.storage.metadata.MetadataHelper;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class MetadataMerger
{

    public void updateMetadataAtVersionLevel(Artifact artifact, ArtifactClient client)
    {
        // TODO: figure out how path should be initialized to retrieve version
        // level metadata given the artifact
        String path = "";
        try
        {
            // If metadata doesn't exits in remote, I will create it
            Metadata metadata = null;
            if (client.pathExists(path))
            {
                InputStream is = client.getResource(path);
                MetadataXpp3Reader reader = new MetadataXpp3Reader();
                metadata = reader.read(is);
            }
            else
            {
                metadata = new Metadata();
                metadata.setGroupId(artifact.getGroupId());
                metadata.setArtifactId(artifact.getArtifactId());
                metadata.setVersion(artifact.getVersion());
            }

            // I generate timestamp once for all the merging
            String timestamp = MetadataHelper.LAST_UPDATED_FIELD_FORMATTER.format(Calendar.getInstance().getTime());

            // Update metadata o fill it for first time in case I have just
            // created it
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

            snapshotVersions
                    .addAll(createNewSnapshotVersions(artifact.getVersion(), timestamp, snapshot.getBuildNumber()));

            // TODO: return the merged metadata to be replaced in remote
        }
        catch (ArtifactTransportException | IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (XmlPullParserException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void updateMetadataAtArtifactLevel(Artifact artifact, ArtifactClient client)
    {
        // TODO: figure out how path should be initialized to retrieve artifact
        // if level metadata given the artifact
        String path = "";
        try
        {
            // If metadata doesn't exits in remote, I will create it
            Metadata metadata = null;
            if (client.pathExists(path))
            {
                InputStream is = client.getResource(path);
                MetadataXpp3Reader reader = new MetadataXpp3Reader();
                metadata = reader.read(is);
            }
            else
            {
                metadata = new Metadata();
                metadata.setGroupId(artifact.getGroupId());
                metadata.setArtifactId(artifact.getArtifactId());
            }

            Versioning versioning = metadata.getVersioning();
            if (versioning == null)
            {
                versioning = new Versioning();
                metadata.setVersioning(versioning);
            }
            versioning.setLatest(artifact.getVersion());
            if (!artifact.isSnapshot())
            {
                versioning.setRelease(artifact.getVersion());
            }
            List<String> versions = versioning.getVersions();
            if (!versions.contains(artifact.getVersion()))
            {
                versions.add(artifact.getVersion());
            }
            versioning.setLastUpdated(
                    MetadataHelper.LAST_UPDATED_FIELD_FORMATTER.format(Calendar.getInstance().getTime()));
            // TODO: return the merged metadata to be replaced in remote
        }
        catch (ArtifactTransportException | IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (XmlPullParserException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void updateMetadataAtPluginLevel(PluginArtifact artifact, ArtifactClient client)
    {
        // TODO: figure out how path should be initialized to retrieve version
        // level metadata given the artifact
        String path = "";
        try
        {
            // If metadata doesn't exits in remote, I will create it
            Metadata metadata = null;
            if (client.pathExists(path))
            {
                InputStream is = client.getResource(path);
                MetadataXpp3Reader reader = new MetadataXpp3Reader();
                metadata = reader.read(is);
            }
            else
            {
                metadata = new Metadata();
            }
            List<Plugin> plugins = metadata.getPlugins();
            boolean found = false;
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
        }
        catch (ArtifactTransportException | IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (XmlPullParserException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
