package org.carlspring.strongbox.storage.services.impl;

import org.carlspring.strongbox.storage.services.ArtifactMetadataService;
import org.carlspring.strongbox.storage.visitors.ArtifactPomVisitor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author stodorov
 */
public class ArtifactMetadataServiceImpl
        implements ArtifactMetadataService
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactMetadataServiceImpl.class);

    private Path basePath = null;

    @Override
    public Metadata getMetadata(Artifact artifact)
            throws IOException, XmlPullParserException
    {
        basePath = getArtifactBasePath(artifact);

        File metadataFile = new File(basePath + "/maven-metadata.xml");
        FileInputStream fileInputStream = new FileInputStream(metadataFile);

        MetadataXpp3Reader reader = new MetadataXpp3Reader();
        return reader.read(fileInputStream);
    }

    @Override
    public void rebuildMetadata(Artifact artifact)
            throws IOException, XmlPullParserException
    {
        String artifactPath = artifact.getGroupId().replaceAll("\\.", Matcher.quoteReplacement(File.separator));
        basePath = Paths.get("target/storages/storage0/releases/" + artifactPath + "/" + artifact.getArtifactId());

        ArtifactPomVisitor artifactPomVisitor = new ArtifactPomVisitor();

        // Find all pom files
        Files.walkFileTree(basePath, artifactPomVisitor);

        // Pass the file list to the metadata generator
        generateMetadata(artifact, artifactPomVisitor.foundPaths);
    }

    public Path getArtifactBasePath(Artifact artifact)
    {
        String artifactPath = artifact.getGroupId().replaceAll("\\.", Matcher.quoteReplacement(File.separator));
        return Paths.get("target/storages/storage0/releases/" + artifactPath + "/" + artifact.getArtifactId());
    }

    private void generateMetadata(Artifact artifact,
                                  ArrayList<Path> foundFiles)
            throws IOException, XmlPullParserException
    {
        if (foundFiles.size() > 0)
        {

            Metadata metadata = new Metadata();
            metadata.setArtifactId(artifact.getArtifactId());
            metadata.setGroupId(artifact.getGroupId());

            Versioning versioning = new Versioning();

            // Latest release path
            Path latestReleasePomFile = null;

            // Add all versions
            for (Path filePath : foundFiles)
            {
                Model model = getPom(filePath);

                if (!filePath.toString().matches("^(.+)-SNAPSHOT.*$"))
                {
                    latestReleasePomFile = filePath;
                }

                versioning.addVersion(model.getVersion());
            }

            // Add latest release version
            Model latestReleasePom = getPom(latestReleasePomFile);
            versioning.setRelease(latestReleasePom.getVersion());

            // Add latest version
            Path latestPomFile = foundFiles.get(foundFiles.size() - 1).toRealPath(LinkOption.NOFOLLOW_LINKS);
            Model latestPom = getPom(latestPomFile);
            versioning.setLatest(latestPom.getVersion());

            metadata.setVersioning(versioning);

            File metadataFile = new File(basePath + "/maven-metadata.xml");
            Writer writer = null;
            try
            {
                writer = WriterFactory.newXmlWriter(metadataFile);

                MetadataXpp3Writer mappingWriter = new MetadataXpp3Writer();

                mappingWriter.write(writer, metadata);

                logger.debug("Maven metadata has been generated for " + artifact.getGroupId() + ":" +
                             artifact.getArtifactId());

            }
            finally
            {
                IOUtil.close(writer);
            }

        }
        else
        {
            throw new IOException("No pom files were founds!");
        }

    }

    private Model getPom(Path filePath)
            throws IOException, XmlPullParserException
    {
        File pomFile = filePath.toRealPath(LinkOption.NOFOLLOW_LINKS).toFile();
        MavenXpp3Reader reader = new MavenXpp3Reader();
        return reader.read(new FileReader(pomFile));
    }

}
