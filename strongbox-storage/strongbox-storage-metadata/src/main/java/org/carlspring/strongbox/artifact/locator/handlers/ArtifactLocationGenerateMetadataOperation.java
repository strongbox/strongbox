package org.carlspring.strongbox.artifact.locator.handlers;

import org.carlspring.maven.commons.io.filters.PomFilenameFilter;
import org.carlspring.strongbox.io.filters.ArtifactVersionDirectoryFilter;
import org.carlspring.strongbox.storage.metadata.MetadataManager;
import org.carlspring.strongbox.storage.metadata.VersionCollectionRequest;
import org.carlspring.strongbox.storage.metadata.VersionCollector;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * @author mtodorov
 */
public class ArtifactLocationGenerateMetadataOperation
        extends AbstractArtifactLocationHandler
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactLocationGenerateMetadataOperation.class);

    private MetadataManager metadataManager;


    public ArtifactLocationGenerateMetadataOperation()
    {
    }

    public ArtifactLocationGenerateMetadataOperation(MetadataManager metadataManager)
    {
        this.metadataManager = metadataManager;
    }

    public void execute(Path path)
    {
        File f = path.toAbsolutePath().toFile();
        List<String> filePaths = Arrays.asList(f.list(new PomFilenameFilter()));

        if (!filePaths.isEmpty() && !getVisitedRootPaths().contains(path.getParent().toString()))
        {
            System.out.println(path.getParent());

            String parentPath = path.getParent().toAbsolutePath().toString();
            if (!getVisitedRootPaths().isEmpty() &&
                !parentPath.startsWith(getVisitedRootPaths().get(getVisitedRootPaths().size() - 1)))
            {
                getVisitedRootPaths().clear();
            }

            getVisitedRootPaths().add(parentPath);

            List<File> versionDirectories = getVersionDirectories(Paths.get(parentPath));
            if (versionDirectories != null)
            {
                VersionCollector versionCollector = new VersionCollector();
                VersionCollectionRequest request;

                request = versionCollector.collectVersions(path.getParent().toAbsolutePath());

                for (File directory : versionDirectories)
                {
                    System.out.println(directory.getAbsolutePath());
                }

                String artifactPath = parentPath.toString().substring(getRepository().getBasedir().length(), parentPath.toString().length());

                // System.out.println("artifactPath = " + artifactPath);

                try
                {
                    metadataManager.generateMetadata(getRepository(), artifactPath, request);
                }
                catch (IOException | XmlPullParserException | NoSuchAlgorithmException e)
                {
                    logger.error("Failed to generate metadata for " + artifactPath);
                    e.printStackTrace();
                }
            }
        }
    }

    public List<File> getVersionDirectories(Path basePath)
    {
        File basedir = basePath.toFile();
        File[] versionDirectories = basedir.listFiles(new ArtifactVersionDirectoryFilter());

        return versionDirectories != null ? Arrays.asList(versionDirectories) : null;
    }

    public MetadataManager getMetadataManager()
    {
        return metadataManager;
    }

    public void setMetadataManager(MetadataManager metadataManager)
    {
        this.metadataManager = metadataManager;
    }

}
