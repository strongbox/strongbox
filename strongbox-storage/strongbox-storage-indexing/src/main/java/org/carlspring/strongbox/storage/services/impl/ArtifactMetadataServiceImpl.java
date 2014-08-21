package org.carlspring.strongbox.storage.services.impl;

import org.carlspring.strongbox.storage.services.ArtifactMetadataService;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import sun.misc.Regexp;

/**
 * @author stodorov
 */
public class ArtifactMetadataServiceImpl implements ArtifactMetadataService
{

    private Path basePath = null;

    @Override
    public Model getMetadata(Artifact artifact)
            throws IOException, XmlPullParserException
    {
//        String artifactPath = artifact.getGroupId().replaceAll("\\.", Matcher.quoteReplacement("/"));
//        File pom = new File("target/storages/storage0/releases/"+artifactPath+"/"+artifact.getArtifactId()+"/maven-metadata.xml");
//        MavenXpp3Reader reader = new MavenXpp3Reader();
//        return reader.read(new FileReader(pom));
        return null;
    }

    @Override
    public void rebuildMetadata(Artifact artifact)
    {
        System.out.println("Artifact Id: " +artifact.getArtifactId());
        System.out.println("Artifact GroupId: " +artifact.getGroupId());
        System.out.println("Replace: "+artifact.getGroupId().replaceAll("\\.", Matcher.quoteReplacement("/")));

        String artifactPath = artifact.getGroupId().replaceAll("\\.", Matcher.quoteReplacement("/"));
        basePath = Paths.get("target/storages/storage0/releases/"+artifactPath+"/"+artifact.getArtifactId());

        ArtifactPomVisitor artifactPomVisitor = new ArtifactPomVisitor();

        try
        {
            // Find all pom files
            Files.walkFileTree(basePath, artifactPomVisitor);
            // Pass the file list to the metadata generator
            generateMetadata(artifact, artifactPomVisitor.foundPaths);
        }
        catch (IOException | XmlPullParserException e)
        {
            e.printStackTrace();
        }
    }

    private void generateMetadata(Artifact artifact, ArrayList<Path> foundFiles)
            throws IOException, XmlPullParserException
    {
        if (foundFiles.size() > 0) {

            Metadata metadata = new Metadata();
            metadata.setArtifactId(artifact.getArtifactId());
            metadata.setGroupId(artifact.getGroupId());

            Versioning versioning = new Versioning();

            // Latest release path
            Path latestReleasePomFile = null;

            // Add all versions
            for (Path filePath : foundFiles) {
                Model model = getPom(filePath);

                if(!filePath.toString().matches("^(.+)-SNAPSHOT.*$"))
                {
                    latestReleasePomFile = filePath;
                }

                versioning.addVersion(model.getVersion());
            }

            // Add latest release version
            Model latestReleasePom = getPom(latestReleasePomFile);
            versioning.setRelease(latestReleasePom.getVersion());

            // Add latest version
            Path latestPomFile = foundFiles.get(foundFiles.size()-1).toRealPath(LinkOption.NOFOLLOW_LINKS);
            Model latestPom = getPom(latestPomFile);
            versioning.setLatest(latestPom.getVersion());

            metadata.setVersioning(versioning);

            File metadataFile = new File(basePath+"/maven-metadata.xml");
            Writer writer = null;
            try
            {
                writer = WriterFactory.newXmlWriter(metadataFile);

                MetadataXpp3Writer mappingWriter = new MetadataXpp3Writer();

                mappingWriter.write( writer, metadata );
            }
            finally
            {
                IOUtil.close(writer);
            }


        } else {
            System.out.println("No files were founds!");
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
