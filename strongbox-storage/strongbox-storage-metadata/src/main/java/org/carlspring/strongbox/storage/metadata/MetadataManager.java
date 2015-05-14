package org.carlspring.strongbox.storage.metadata;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.services.BasicRepositoryService;
import org.carlspring.strongbox.storage.metadata.comparators.SnapshotVersionComparator;
import org.carlspring.strongbox.storage.metadata.comparators.VersionComparator;
import org.carlspring.strongbox.storage.metadata.versions.MetadataVersion;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryPolicyEnum;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * @author stodorov
 */
@Component
public class MetadataManager
{

    private static final Logger logger = LoggerFactory.getLogger(MetadataManager.class);

    @Autowired
    private BasicRepositoryService basicRepositoryService;


    public MetadataManager()
    {
    }

    /**
     * Returns artifact metadata instance
     *
     * @param artifactBasePath Path
     * @return Metadata
     * @throws java.io.IOException
     * @throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
     */
    public Metadata getMetadata(Path artifactBasePath)
            throws IOException, XmlPullParserException
    {
        File metadataFile = getMetadataFile(artifactBasePath);
        Metadata metadata = null;
        FileInputStream fis = null;

        try
        {
            fis = new FileInputStream(metadataFile);

            MetadataXpp3Reader reader = new MetadataXpp3Reader();

            metadata = reader.read(fis);
        }
        finally
        {
            ResourceCloser.close(fis, logger);
        }

        return metadata;
    }

    public Metadata getMetadata(Repository repository, Artifact artifact)
            throws IOException, XmlPullParserException
    {
        Metadata metadata = null;

        if (basicRepositoryService.containsArtifact(repository, artifact))
        {
            Path artifactPath = Paths.get(basicRepositoryService.getPathToArtifact(repository, artifact));
            Path artifactBasePath = artifactPath;
            if (artifact.getVersion() != null)
            {
                artifactBasePath = artifactPath.getParent().getParent();
            }

            logger.debug("Getting metadata for " + artifactBasePath.toAbsolutePath());

            metadata = getMetadata(artifactBasePath);
        }
        else
        {
            throw new IOException("Artifact " + artifact.toString() + " does not exist in " + repository.getStorage().getBasedir() +"/" + repository.getBasedir() + " !");
        }

        return metadata;
    }

    /**
     * Returns artifact metadata File
     *
     * @param artifactBasePath Path
     * @return File
     */
    public File getMetadataFile(Path artifactBasePath)
            throws FileNotFoundException
    {
        if (artifactBasePath.toFile().exists())
        {
            return new File(artifactBasePath.toFile().getAbsolutePath() + "/maven-metadata.xml");
        }
        else
        {
            throw new FileNotFoundException("Could not find " + new File(artifactBasePath.toFile().getAbsolutePath() + "/maven-metadata.xml") + "!");
        }
    }

    /**
     * Generate a metadata file for an artifact.
     *
     * @param repository Repository
     * @throws IOException
     * @throws XmlPullParserException
     * @throws NoSuchAlgorithmException
     */
    public void generateMetadata(Repository repository, String path, VersionCollectionRequest request)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        if (basicRepositoryService.containsPath(repository, path))
        {
            logger.debug("Artifact metadata generation triggered for " + path +
                         " in '" + repository.getStorage().getId() + ":" + repository.getId() + "'" +
                         " [policy: " + repository.getPolicy() + "].");

            Artifact artifact = ArtifactUtils.convertPathToArtifact(path);

            Metadata metadata = new Metadata();
            metadata.setArtifactId(artifact.getArtifactId());
            metadata.setGroupId(artifact.getGroupId());

            List<MetadataVersion> baseVersioning = request.getMetadataVersions();
            Versioning versioning = request.getVersioning();

            // Set lastUpdated tag for main maven-metadata
            versioning.setLastUpdated(String.valueOf(Instant.now().getEpochSecond()));

            /**
             * In a release repository we only need to generate maven-metadata.xml in the artifactBasePath
             * (i.e. org/foo/bar/maven-metadata.xml)
             */
            if (repository.getPolicy().equals(RepositoryPolicyEnum.RELEASE.getPolicy()))
            {
                // Don't write empty <versioning/> tags when no versions are available.
                if (!versioning.getVersions().isEmpty())
                {
                    String latestVersion = baseVersioning.get(baseVersioning.size() - 1).getVersion();

                    metadata.setVersioning(request.getVersioning());
                    versioning.setRelease(latestVersion);

                    // Set <latest> by figuring out the most recent upload
                    Collections.sort(baseVersioning);
                    versioning.setLatest(latestVersion);
                }

                // Write basic metadata
                writeMetadata(request.getArtifactBasePath(), metadata);

                logger.debug("Generated Maven metadata for " +
                             artifact.getGroupId() + ":" +
                             artifact.getArtifactId() + ".");
            }
            /**
             * In a snapshot repository we need to generate maven-metadata.xml in the artifactBasePath and
             * generate additional maven-metadata.xml files for each snapshot directory containing information about
             * all available artifacts.
             */
            else if (repository.getPolicy().equals(RepositoryPolicyEnum.SNAPSHOT.getPolicy()))
            {
                // Don't write empty <versioning/> tags when no versions are available.
                if (!versioning.getVersions().isEmpty())
                {
                    // Set <latest>
                    String latestVersion = versioning.getVersions().get(versioning.getVersions().size()-1);
                    versioning.setLatest(latestVersion);

                    metadata.setVersioning(versioning);

                    // Generate and write additional snapshot metadata.
                    for (String version : metadata.getVersioning().getVersions())
                    {
                        Path snapshotBasePath = Paths.get(request.getArtifactBasePath().toAbsolutePath() + "/" +
                                                          ArtifactUtils.getSnapshotBaseVersion(version));

                        VersionCollector versionCollector = new VersionCollector();
                        List<SnapshotVersion> snapshotVersions = versionCollector.collectTimestampedSnapshotVersions(snapshotBasePath);

                        // Write snapshot metadata version information for each snapshot.
                        Metadata snapshotMetadata = new Metadata();
                        snapshotMetadata.setArtifactId(artifact.getArtifactId());
                        snapshotMetadata.setGroupId(artifact.getGroupId());

                        Versioning snapshotVersioning = versionCollector.generateSnapshotVersions(snapshotVersions);
                        if (!snapshotVersioning.getSnapshotVersions().isEmpty())
                        {
                            SnapshotVersion latestSnapshot = snapshotVersioning.getSnapshotVersions().get(snapshotVersioning.getSnapshotVersions().size() - 1);

                            String timestamp = ArtifactUtils.getSnapshotTimestamp(latestSnapshot.getVersion());
                            // Potentially revisit this for timestamps with custom formats
                            int buildNumber = Integer.parseInt(ArtifactUtils.getSnapshotBuildNumber(latestSnapshot.getVersion()));

                            if (!StringUtils.isEmpty(timestamp) || !StringUtils.isEmpty(buildNumber))
                            {
                                Snapshot snapshotVersion = new Snapshot();

                                snapshotVersion.setTimestamp(timestamp);
                                snapshotVersion.setBuildNumber(buildNumber);

                                snapshotVersioning.setSnapshot(snapshotVersion);
                            }

                        }

                        // Last updated should be present in both cases.
                        snapshotVersioning.setLastUpdated(String.valueOf(Instant.now().getEpochSecond()));

                        snapshotMetadata.setVersioning(snapshotVersioning);

                        // Set the version that this metadata represents, if any. This is used for artifact snapshots only.
                        // http://maven.apache.org/ref/3.2.1/maven-repository-metadata/repository-metadata.html
                        snapshotMetadata.setVersion(version);

                        writeMetadata(snapshotBasePath, snapshotMetadata);
                    }
                }

                // Write basic metadata
                writeMetadata(request.getArtifactBasePath(), metadata);

                logger.debug("Generated Maven metadata for " + artifact.getGroupId() + ":" +
                             artifact.getArtifactId() + ".");
            }
            else if (repository.getPolicy().equals(RepositoryPolicyEnum.MIXED.getPolicy()))
            {
                // TODO: Implement merging.
            }
            else
            {
                throw new RuntimeException("Repository policy type unknown: " + repository.getId());
            }

            // If this is a plugin, we need to add an additional metadata to the groupId.artifactId path.
            if (!request.getPlugins().isEmpty())
            {
                Metadata pluginMetadata = new Metadata();
                pluginMetadata.setPlugins(request.getPlugins());

                Path pluginMetadataPath = request.getArtifactBasePath().getParent();

                writeMetadata(pluginMetadataPath, pluginMetadata);

                logger.debug("Generated Maven plugin metadata for " + artifact.getGroupId() + ":" +
                             artifact.getArtifactId() + ".");
            }
        }
        else
        {
            logger.error("Artifact metadata generation failed: " + path + ").");
        }
    }

    /**
     * Merge the existing metadata file of an artifact with the incoming new metadata.
     *
     * @param repository    Repository
     * @param artifact      Artifact
     * @param mergeMetadata Metadata
     * @throws IOException
     * @throws XmlPullParserException
     */
    public void mergeMetadata(Repository repository, Artifact artifact, Metadata mergeMetadata)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        if (basicRepositoryService.containsArtifact(repository, artifact))
        {
            Path artifactBasePath;
            if (artifact.getFile() != null && !artifact.getFile().isDirectory())
            {
                artifactBasePath = artifact.getFile().toPath().getParent().getParent();
            }
            else
            {
                artifactBasePath = artifact.getFile().toPath();
            }

            logger.debug("Artifact merge metadata triggered for " + artifact.toString() +
                         "(" +artifactBasePath + "). " + repository.getType());

            try
            {
                Metadata metadata = getMetadata(repository, artifact);
                metadata.merge(mergeMetadata);

                Versioning versioning = metadata.getVersioning();
                if (versioning.getVersions() != null)
                {
                    Collections.sort(versioning.getVersions(), new VersionComparator());
                }
                if (versioning.getSnapshotVersions() != null)
                {
                    Collections.sort(versioning.getSnapshotVersions(), new SnapshotVersionComparator());
                }

                writeMetadata(artifactBasePath, metadata);
            }
            catch (FileNotFoundException e)
            {
                throw new IOException("Artifact " + artifact.toString() + " doesn't contain any metadata," +
                                      " therefore we can't merge the metadata!");
            }
        }
        else
        {
            throw new IOException("Artifact " + artifact.toString() + " does not exist in " +
                                  repository.getStorage().getBasedir() +"/" + repository.getBasedir() + " !");
        }
    }

    private void writeMetadata(Path metadataBasePath, Metadata metadata)
            throws IOException,
                   NoSuchAlgorithmException
    {
        File metadataFile = getMetadataFile(metadataBasePath);

        OutputStream os = null;
        Writer writer = null;

        try
        {
            os = new MultipleDigestOutputStream(metadataFile, new FileOutputStream(metadataFile));

            writer = WriterFactory.newXmlWriter(os);
            MetadataXpp3Writer mappingWriter = new MetadataXpp3Writer();
            mappingWriter.write(writer, metadata);
        }
        finally
        {
            ResourceCloser.close(writer, logger);
            ResourceCloser.close(os, logger);
        }
    }

}
