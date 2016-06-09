package org.carlspring.strongbox.storage.metadata;

import org.carlspring.maven.commons.DetachedArtifact;
import org.carlspring.maven.commons.util.ArtifactUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.springframework.util.StringUtils;

/**
 * @author mtodorov
 */
public class MetadataHelper
{

    public static final SimpleDateFormat LAST_UPDATED_FIELD_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");


    public static void setLastUpdated(Versioning versioning)
    {
        if (versioning != null)
        {
            versioning.setLastUpdated(LAST_UPDATED_FIELD_FORMATTER.format(Calendar.getInstance().getTime()));
        }
    }

    public static void setLatest(Metadata metadata)
    {
        setLatest(metadata, null);
    }

    /**
     * Sets the "latest" field.
     *
     * @param metadata          The metadata to apply this to.
     * @param currentLatest     Only pass this in, if this is delete mode, otherwise
     *                          this method will figure things out on it's own.
     */
    public static void setLatest(Metadata metadata, String currentLatest)
    {
        Versioning versioning = metadata.getVersioning() != null ? metadata.getVersioning() : new Versioning();
        if (metadata.getVersioning() == null)
        {
            metadata.setVersioning(versioning);
        }

        List<String> versions = versioning.getVersions();

        sortVersions(versions);

        if (currentLatest != null &&
            versioning.getLatest() != null && versioning.getLatest().equals(currentLatest))
        {
            // Delete mode:
            if (versions.size() > 1)
            {
                // TODO: Is this the right thing to do?
                versioning.setLatest(versions.get(versions.size() - 2));
            }
            else
            {
                // TODO: Figure out what we should do in case there are no other available versions
            }
        }
        else
        {
            // Regular mode
            versioning.setLatest(versions.get(versions.size() - 1));
        }
    }

    /**
     * @param metadata          The metadata to apply this to.
     */
    public static void setRelease(Metadata metadata)
    {
        setRelease(metadata, null);
    }

    /**
     * Sets the "release" field.
     *
     * @param metadata          The metadata to apply this to.
     * @param currentRelease    Only pass this in, if this is delete mode, otherwise
     *                          this method will figure things out on it's own.
     */
    public static void setRelease(Metadata metadata, String currentRelease)
    {
        Versioning versioning = metadata.getVersioning() != null ? metadata.getVersioning() : new Versioning();
        if (metadata.getVersioning() == null)
        {
            metadata.setVersioning(versioning);
        }

        List<String> versions = versioning.getVersions();

        sortVersions(versions);

        if (currentRelease != null &&
            versioning.getRelease()!= null && versioning.getRelease().equals(currentRelease))
        {
            // Delete mode:
            if (versions.size() > 1)
            {
                versioning.setRelease(versions.get(versions.size() - 2));
            }
            else
            {
                // TODO: Figure out what we should do in case there are no other available versions
            }
        }
        else
        {
            // Regular mode
            versioning.setRelease(versions.get(versions.size() - 1));
        }
    }

    private static void sortVersions(List<String> versions)
    {
        // Sort the versions in order to set <release> by figuring out the most recent upload
        if (versions != null)
        {
            Collections.sort(versions);
        }
    }

    public static SnapshotVersion createSnapshotVersion(String groupId,
                                                        String artifactId,
                                                        String version,
                                                        String classifier,
                                                        String extension)
    {
        Artifact artifact = new DetachedArtifact(groupId, artifactId, version, null, classifier);

        return createSnapshotVersion(artifact, extension);
    }

    public static SnapshotVersion createSnapshotVersion(Artifact artifact, String extension)
    {
        SnapshotVersion snapshotVersion = new SnapshotVersion();
        snapshotVersion.setVersion(artifact.getVersion());
        snapshotVersion.setExtension(extension);
        snapshotVersion.setUpdated(MetadataHelper.LAST_UPDATED_FIELD_FORMATTER.format(Calendar.getInstance().getTime()));

        if (artifact.getClassifier() != null)
        {
            snapshotVersion.setClassifier(artifact.getClassifier());
        }

        return snapshotVersion;
    }

    public static void setupSnapshotVersioning(Versioning snapshotVersioning)
    {
        if (!snapshotVersioning.getSnapshotVersions().isEmpty())
        {
            SnapshotVersion latestSnapshot = snapshotVersioning.getSnapshotVersions().get(snapshotVersioning.getSnapshotVersions().size() - 1);

            String timestamp = ArtifactUtils.getSnapshotTimestamp(latestSnapshot.getVersion());
            // Potentially revisit this for timestamps with custom formats
            int buildNumber = Integer.parseInt(ArtifactUtils.getSnapshotBuildNumber(latestSnapshot.getVersion()));

            if (!StringUtils.isEmpty(timestamp) || !StringUtils.isEmpty(buildNumber))
            {
                Snapshot snapshot = new Snapshot();

                snapshot.setTimestamp(timestamp);
                snapshot.setBuildNumber(buildNumber);

                snapshotVersioning.setSnapshot(snapshot);
            }
        }
    }

    public static boolean containsTimestampedSnapshotVersion(Metadata metadata,
                                                             String timestampedSnapshotVersion)
    {
        return containsTimestampedSnapshotVersion(metadata, timestampedSnapshotVersion, null);
    }

    public static boolean containsTimestampedSnapshotVersion(Metadata metadata,
                                                             String timestampedSnapshotVersion,
                                                             String classifier)
    {
        for (SnapshotVersion snapshotVersion : metadata.getVersioning().getSnapshotVersions())
        {
            if (snapshotVersion.getVersion().equals(timestampedSnapshotVersion))
            {
                if (classifier == null || snapshotVersion.getClassifier().equals(classifier))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean containsVersion(Metadata metadata,
                                          String version)
    {
        return metadata.getVersioning().getVersions().contains(version);
    }

    /**
     * Returns artifact metadata File
     *
     * @param artifactBasePath Path
     * @return File
     */
    public static File getMetadataFile(Path artifactBasePath)
            throws FileNotFoundException
    {
        if (artifactBasePath.toFile().exists())
        {
            return new File(artifactBasePath.toFile().getAbsolutePath() + "/maven-metadata.xml");
        }
        else
        {
            throw new FileNotFoundException("Could not find " +
                                            new File(artifactBasePath.toFile().getAbsolutePath() + "/maven-metadata.xml") + "!");
        }
    }

    public static File getArtifactMetadataFile(Path artifactBasePath)
    {
        return getMetadataFile(artifactBasePath, null, MetadataType.ARTIFACT_ROOT_LEVEL);
    }

    public static File getSnapshotMetadataFile(Path artifactBasePath, String version)
    {
        return getMetadataFile(artifactBasePath, version, MetadataType.SNAPSHOT_VERSION_LEVEL);
    }

    public static File getPluginMetadataFile(Path artifactBasePath)
    {
        return getMetadataFile(artifactBasePath, null, MetadataType.PLUGIN_GROUP_LEVEL);
    }

    /**
     * Returns artifact metadata File
     *
     * @param artifactBasePath Path
     * @return File
     */
    public static File getMetadataFile(Path artifactBasePath, String version, MetadataType metadataType)
    {
        switch (metadataType)
        {
            case PLUGIN_GROUP_LEVEL:
                return new File(artifactBasePath.getParent().toFile().getAbsolutePath() + "/maven-metadata.xml");
            case SNAPSHOT_VERSION_LEVEL:
                return new File(artifactBasePath.toFile().getAbsolutePath() + "/" + version + "/maven-metadata.xml");
            case ARTIFACT_ROOT_LEVEL:
            default:
                return new File(artifactBasePath.toFile().getAbsolutePath() + "/maven-metadata.xml");
        }
    }

    public static Path getMetadataPath(Path artifactBasePath, String version, MetadataType metadataType)
    {
        return getMetadataFile(artifactBasePath, version, metadataType).toPath();
    }

}
