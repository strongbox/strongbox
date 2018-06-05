package org.carlspring.strongbox.artifact.locator.handlers;

import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.RepositoryLayoutFileSystemProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kate Novik.
 */
public class ArtifactLocationGenerateChecksumOperation
        extends AbstractArtifactLocationHandler
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactLocationGenerateChecksumOperation.class);

    private Path previousPath;

    private boolean forceRegeneration = false;

    public void execute(RepositoryPath path)
            throws IOException
    {
        try (Stream<Path> pathStream = Files.list(path))
        {
            boolean containsMetadata = pathStream.anyMatch(p -> {
                try
                {
                    return RepositoryFiles.isMetadata((RepositoryPath) p);
                }
                catch (IOException e)
                {
                    logger.error(String.format("Failed to read attributes for [%s]", p),
                                 e);
                }
                return false;
            });
            if (!containsMetadata) {
                logger.debug(String.format("Target path [%s] does not contains any metadata, so we don't need to execute any operations.",
                                           path));
                return;
            }
        }

        RepositoryPath parentPath = path;

        // Don't enter visited paths (i.e. version directories such as 1.2, 1.3, 1.4...)
        if (!getVisitedRootPaths().isEmpty() && getVisitedRootPaths().containsKey(parentPath))
        {
            List<RepositoryPath> visitedVersionPaths = getVisitedRootPaths().get(parentPath);

            if (visitedVersionPaths.contains(path))
            {
                return;
            }
        }

        if (logger.isDebugEnabled())
        {
            // We're using System.out.println() here for clarity and due to the length of the lines
            System.out.println(parentPath);
        }

        // The current directory is out of the tree
        if (previousPath != null && !parentPath.startsWith(previousPath))
        {
            getVisitedRootPaths().remove(previousPath);
            previousPath = parentPath;
        }

        if (previousPath == null)
        {
            previousPath = parentPath;
        }

        RepositoryPath basePath = parentPath;
        RepositoryLayoutFileSystemProvider provider = (RepositoryLayoutFileSystemProvider) basePath.getFileSystem()
                                                                                                   .provider();
        provider.storeChecksum(basePath, forceRegeneration);
    }

    public boolean getForceRegeneration()
    {
        return forceRegeneration;
    }

    public void setForceRegeneration(boolean forceRegeneration)
    {
        this.forceRegeneration = forceRegeneration;
    }
}
