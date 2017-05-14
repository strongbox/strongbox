package org.carlspring.strongbox.artifact.locator.handlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.RepositoryLayoutFileSystemProvider;
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
        List<Path> filePaths = Files.list(path)
                                      .filter(p -> {
                                        try
                                        {
                                            return Boolean.TRUE.equals(Files.getAttribute(p,
                                                                                                  RepositoryFileAttributes.METADATA));
                                        }
                                        catch (IOException e)
                                        {
                                            logger.error(String.format("Failed to read attributes for [%s]", p), e);
                                        }
                                        return false;
                                    }).collect(Collectors.toList());
        
        RepositoryPath parentPath = path.getParent()
                                        .toAbsolutePath();

        if (filePaths.isEmpty())
        {
            return;
        }
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

        List<RepositoryPath> versionDirectories = getVersionDirectories(parentPath);
        if (versionDirectories == null || versionDirectories.isEmpty())
        {
            return;
        }
        
        getVisitedRootPaths().put(parentPath, versionDirectories);
        if (logger.isDebugEnabled())
        {
            for (Path directory : versionDirectories)
            {
                // We're using System.out.println() here for clarity and due to the length of the lines
                System.out.println(" " + directory.toAbsolutePath().toString());
            }
        }

        RepositoryPath basePath = versionDirectories.get(0).getParent();
        RepositoryLayoutFileSystemProvider provider = (RepositoryLayoutFileSystemProvider) basePath.getFileSystem()
                                                                                                   .provider();
        try
        {
            provider.storeChecksum(basePath, forceRegeneration);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IOException(e);
        }
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
