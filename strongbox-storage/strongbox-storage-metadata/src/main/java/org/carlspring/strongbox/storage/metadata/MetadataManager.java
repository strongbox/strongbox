package org.carlspring.strongbox.storage.metadata;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
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

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
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
            logger.debug("Getting metadata for " + Paths.get(basicRepositoryService.getPathToArtifact(repository, artifact)).toString());

            Path artifactPath = Paths.get(basicRepositoryService.getPathToArtifact(repository, artifact));
            Path artifactBasePath = artifactPath.getParent().getParent();

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
     * @throws NullPointerException
     */
    public File getMetadataFile(Path artifactBasePath)
            throws FileNotFoundException, NullPointerException
    {
        if(artifactBasePath.toFile().exists())
        {
            return new File(artifactBasePath.toFile().getAbsolutePath() + "/maven-metadata.xml");
        }
        else
        {
            throw new FileNotFoundException();
        }
    }

    /**
     * Generate a metadata file for an artifact.
     *
     * @param repository Repository
     * @param artifact   Artifact
     * @throws IOException
     * @throws XmlPullParserException
     */
    public void generateMetadata(Repository repository, Artifact artifact)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        if (basicRepositoryService.containsArtifact(repository, artifact))
        {
            logger.debug("Artifact metadata generation triggered for " + artifact.toString() + ". "+repository.getType());

            Path artifactBasePath = artifact.getFile().toPath().getParent().getParent();

            Metadata metadata = new Metadata();
            metadata.setArtifactId(artifact.getArtifactId());
            metadata.setGroupId(artifact.getGroupId());

            VersionCollector versionCollector = new VersionCollector();

            List<MetadataVersion> baseVersioning = versionCollector.collectVersions(artifactBasePath);
            Versioning versioning = versionCollector.generateVersioning(baseVersioning);

            /**
             * In a release repository we only need to generate maven-metadata.xml in the artifactBasePath
             * (i.e. org/foo/bar/maven-metadata.xml)
             */
            if (repository.getPolicy().equals(RepositoryPolicyEnum.RELEASE.getPolicy()))
            {
                // Don't write empty <versioning/> tags when no versions are available.
                if (!versioning.getVersions().isEmpty())
                {
                    metadata.setVersioning(versioning);
                    versioning.setRelease(baseVersioning.get(baseVersioning.size() - 1).getVersion());
                }

                // Write basic metadata
                writeMetadata(artifactBasePath, metadata);

                logger.debug("Generated basic maven metadata for " + artifact.getGroupId() + ":" +
                             artifact.getArtifactId() + ".");

            }
            /**
             * In a snapshot repository we need to generate maven-metadata.xml in the artifactBasePath and
             * generate additional maven-metadata.xml files for each snapshot directory containing information about
             * all available artifacts.
             */
            else if(repository.getPolicy().equals(RepositoryPolicyEnum.SNAPSHOT.getPolicy()))
            {
                // Don't write empty <versioning/> tags when no versions are available.
                if(versioning.getVersions().size() > 0)
                {
                    metadata.setVersioning(versioning);

                    // Generate and write additional snapshot metadata.
                    for(String version : metadata.getVersioning().getVersions())
                    {
                        Path snapshotBasePath = Paths.get(artifactBasePath.toAbsolutePath() + "/" + version);

                        // Write snapshot metadata version information for each snapshot.
                        Metadata snapshotMetadata = new Metadata();
                        snapshotMetadata.setArtifactId(artifact.getArtifactId());
                        snapshotMetadata.setGroupId(artifact.getGroupId());

                        List<SnapshotVersion> snapshotVersions = versionCollector.collectSnapshotVersions(snapshotBasePath);

                        Collections.sort(snapshotVersions, new SnapshotVersionComparator());

                        snapshotMetadata.setVersioning(versionCollector.generateSnapshotVersions(snapshotVersions));

                        writeMetadata(snapshotBasePath, snapshotMetadata);
                    }
                }

                // Write basic metadata
                writeMetadata(artifactBasePath, metadata);

                logger.debug("Generated basic maven metadata for " + artifact.getGroupId() + ":" +
                             artifact.getArtifactId() + ".");
            }
            else if(repository.getPolicy().equals(RepositoryPolicyEnum.MIXED.getPolicy()))
            {
                // TODO: Implement merging.
            }
            else
            {
                throw new RuntimeException("Repository policy type unknown: "+repository.getId());
            }

            /*
            for (int i = 0; i < metadata.generateVersioning().getVersions().size(); i++)
            {
                logger.debug("Version: "+metadata.generateVersioning().getVersions().get(i));
            }
            */

            // If this is a plugin, we need to add an additional metadata to the groupId.artifactId path.
            if (versionCollector.getPlugins() != null && versionCollector.getPlugins().size() > 0)
            {
                Metadata pluginMetadata = new Metadata();
                pluginMetadata.setPlugins(versionCollector.getPlugins());

                Path pluginMetadataPath = artifactBasePath.getParent();

                writeMetadata(pluginMetadataPath, pluginMetadata);

                logger.debug("Generated Maven plugin metadata for " + artifact.getGroupId() + ":" +
                             artifact.getArtifactId() + ".");
            }
        }
        else
        {
            logger.debug("Artifact metadata generation failed: artifact missing (" + artifact.toString() + ")");
        }
    }

    public void generateMetadata(Repository repository, String artifactPath)
            throws IOException,
                   XmlPullParserException,
                   NoSuchAlgorithmException
    {
        generateMetadata(repository, ArtifactUtils.convertPathToArtifact(artifactPath));
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
            logger.debug("Artifact merge metadata triggered for " + artifact.toString() + ". " + repository.getType());

            Path artifactBasePath = artifact.getFile().toPath().getParent().getParent();

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
