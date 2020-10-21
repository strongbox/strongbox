package org.carlspring.strongbox.locator.handlers;

import org.carlspring.strongbox.artifact.locator.handlers.AbstractArtifactLocationHandler;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.storage.metadata.VersionCollectionRequest;
import org.carlspring.strongbox.storage.metadata.VersionCollector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public abstract class AbstractMavenArtifactLocatorOperation
        extends AbstractArtifactLocationHandler
{

    private static final Logger logger = LoggerFactory.getLogger(AbstractMavenArtifactLocatorOperation.class);

    private RepositoryPath previousPath;


    public AbstractMavenArtifactLocatorOperation()
    {
    }

    public void execute(RepositoryPath direcotryPath)
            throws IOException
    {
        List<Path> pomFiles;
        try (Stream<Path> pathStream = Files.list(direcotryPath))
        {
            pomFiles = pathStream.filter(p -> p.getFileName().toString().endsWith(".pom")).sorted().collect(
                    Collectors.toList());
        }

        if (pomFiles.isEmpty())
        {
            return;
        }
        
        RepositoryPath artifactGroupDirectoryPath = direcotryPath.getParent();
        
        // Don't enter visited paths (i.e. version directories such as 1.2, 1.3, 1.4...)
        if (getVisitedRootPaths().containsKey(artifactGroupDirectoryPath) && getVisitedRootPaths().get(artifactGroupDirectoryPath).contains(direcotryPath))
        {
            return;
        }

        if (logger.isDebugEnabled())
        {
            // We're using System.out.println() here for clarity and due to the length of the lines
            System.out.println(artifactGroupDirectoryPath);
        }

        // The current directory is out of the tree
        if (previousPath != null && !artifactGroupDirectoryPath.startsWith(previousPath))
        {
            getVisitedRootPaths().remove(previousPath);
            previousPath = artifactGroupDirectoryPath;
        }

        if (previousPath == null)
        {
            previousPath = artifactGroupDirectoryPath;
        }

        List<RepositoryPath> versionDirectories = getVersionDirectories(artifactGroupDirectoryPath);
        if (versionDirectories == null)
        {
            return;
        }
        getVisitedRootPaths().put(artifactGroupDirectoryPath, versionDirectories);

        VersionCollector versionCollector = new VersionCollector();
        VersionCollectionRequest request = versionCollector.collectVersions(artifactGroupDirectoryPath.toAbsolutePath());

        if (logger.isDebugEnabled())
        {
            for (RepositoryPath directory : versionDirectories)
            {
                // We're using System.out.println() here for clarity and due to the length of the lines
                System.out.println(" " + directory.toAbsolutePath());
            }
        }

        executeOperation(request, artifactGroupDirectoryPath, versionDirectories);
    }

    public abstract void executeOperation(VersionCollectionRequest request,
                                          RepositoryPath artifactGroupDirectoryPath,
                                          List<RepositoryPath> versionDirectories)
            throws IOException;

}
