package org.carlspring.strongbox.storage.metadata;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.services.BasicRepositoryService;
import org.carlspring.strongbox.storage.metadata.visitors.ArtifactPomVisitor;
import org.carlspring.strongbox.storage.repository.Repository;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

/**
 * @author stodorov
 */
@Component
public class MetadataManager
{

    @Autowired
    private BasicRepositoryService basicRepositoryService;

    private static final Logger logger = LoggerFactory.getLogger(MetadataManager.class);


    public MetadataManager()
    {
    }

    /**
     * Returns artifact metadata instance
     *
     * @param artifactBasePath Path
     *
     * @return Metadata
     *
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

    /**
     * Returns artifact metadata File
     *
     * @param artifactBasePath Path
     *
     * @return File
     *
     * @throws NullPointerException
     */
    public File getMetadataFile(Path artifactBasePath)
            throws NullPointerException
    {
        return new File(artifactBasePath.toFile().getAbsolutePath() + "/maven-metadata.xml");
    }

    /**
     * Generate a metadata file for an artifact.
     *
     * @param repository Repository
     * @param artifact   Artifact
     *
     * @throws IOException
     * @throws XmlPullParserException
     */
    public void generateMetadata(Repository repository, Artifact artifact)
            throws IOException, XmlPullParserException
    {
        if (basicRepositoryService.containsArtifact(repository, artifact))
        {
            logger.debug("Artifact metadata generation triggered for " + artifact.toString() + ".");

            ArtifactPomVisitor artifactPomVisitor = new ArtifactPomVisitor();

            Path artifactBasePath = artifact.getFile().toPath().getParent().getParent();

            // Find all artifact versions
            Files.walkFileTree(artifactBasePath, artifactPomVisitor);

            List<Path> foundFiles = artifactPomVisitor.getMatchingPaths();

            if(foundFiles.size() > 0)
            {
                Metadata metadata = new Metadata();
                metadata.setArtifactId(artifact.getArtifactId());
                metadata.setGroupId(artifact.getGroupId());

                VersionCollector versionCollector = new VersionCollector();
                versionCollector.processPomFiles(foundFiles);

                // Write artifact metadata if there is any.
                if(versionCollector.getVersioning() != null &&
                   (versionCollector.getVersioning().getVersions().size() > 0 ||
                    versionCollector.getVersioning().getSnapshotVersions().size() > 0))
                {
                    metadata.setVersioning(versionCollector.getVersioning());
                    metadata.getVersioning().setLastUpdatedTimestamp(new Date());

                    for (int i = 0; i < metadata.getVersioning().getVersions().size(); i++)
                    {
                        logger.debug("Version: "+metadata.getVersioning().getVersions().get(i));
                    }
                    
                    writeMetadata(artifactBasePath, metadata);

                    logger.debug("Generated Maven metadata for " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ".");
                }

                // Write plugin metadata if there is any.
                if(versionCollector.getPlugins() != null && versionCollector.getPlugins().size() > 0)
                {
                    Metadata pluginMetadata = new Metadata();
                    pluginMetadata.setPlugins(versionCollector.getPlugins());

                    Path pluginMetadataPath = artifactBasePath.getParent();

                    writeMetadata(pluginMetadataPath, pluginMetadata);

                    logger.debug("Generated Maven plugin metadata for " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ".");
                }
            }
            else
            {
                logger.debug("No artifacts found.");
            }
        }
        else
        {
            logger.debug("Artifact metadata generation failed: artifact missing (" + artifact.toString() + ")");
        }
    }

    public void generateMetadata(Repository repository, String artifactPath)
            throws IOException, XmlPullParserException
    {
        generateMetadata(repository, ArtifactUtils.convertPathToArtifact(artifactPath));
    }

    /**
     * Merge the existing metadata file of an artifact with the incoming new metadata.
     *
     * @param artifactBasePath Path
     * @param mergeMetadata    Metadata
     */
    public void mergeMetadata(Path artifactBasePath, Metadata mergeMetadata)
            throws IOException, XmlPullParserException
    {

        Metadata metadata = getMetadata(artifactBasePath);
        metadata.merge(mergeMetadata);

        File metadataFile = getMetadataFile(artifactBasePath);
        Writer writer = null;

        try
        {
            writer = WriterFactory.newXmlWriter(metadataFile);

            MetadataXpp3Writer mappingWriter = new MetadataXpp3Writer();

            mappingWriter.write(writer, metadata);

            logger.debug("Merged Maven metadata for " + metadata.getGroupId() + ":" + metadata.getArtifactId() + ".");
        }
        finally
        {
            ResourceCloser.close(writer, logger);
        }
    }

    private void writeMetadata(Path metadataBasePath, Metadata metadata)
            throws IOException
    {
        File metadataFile = getMetadataFile(metadataBasePath);
        Writer writer = null;

        try
        {
            writer = WriterFactory.newXmlWriter(metadataFile);
            MetadataXpp3Writer mappingWriter = new MetadataXpp3Writer();
            mappingWriter.write(writer, metadata);
        }
        finally
        {
            ResourceCloser.close(writer, logger);
        }
    }

}
