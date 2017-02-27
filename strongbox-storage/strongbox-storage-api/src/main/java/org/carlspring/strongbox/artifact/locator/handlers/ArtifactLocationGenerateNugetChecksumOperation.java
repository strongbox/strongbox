package org.carlspring.strongbox.artifact.locator.handlers;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.filters.MetadataFilenameFilter;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.storage.checksum.NugetChecksumManager;
import org.carlspring.strongbox.storage.repository.UnknownRepositoryTypeException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Kate Novik.
 */
public class ArtifactLocationGenerateNugetChecksumOperation
        extends AbstractArtifactLocationHandler
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactLocationGenerateNugetChecksumOperation.class);

    private String previousPath;

    private boolean forceRegeneration = false;

    private NugetChecksumManager nugetChecksumManager;

    private LayoutProviderRegistry layoutProviderRegistry;


    public ArtifactLocationGenerateNugetChecksumOperation(NugetChecksumManager nugetChecksumManager)
    {
        this.nugetChecksumManager = nugetChecksumManager;
    }

    public void execute(Path path)
    {
        File f = path.toAbsolutePath()
                     .toFile();

        String[] list = f.list(new MetadataFilenameFilter());
        List<String> filePaths = list != null ? Arrays.asList(list) : new ArrayList<>();

        String parentPath = path.getParent()
                                .toAbsolutePath()
                                .toString();

        if (!filePaths.isEmpty())
        {
            // Don't enter visited paths (i.e. version directories such as 1.2, 1.3, 1.4...)
            if (!getVisitedRootPaths().isEmpty() && getVisitedRootPaths().containsKey(parentPath))
            {
                List<File> visitedVersionPaths = getVisitedRootPaths().get(parentPath);

                if (visitedVersionPaths.contains(f))
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

            List<File> versionDirectories = getVersionDirectories(Paths.get(parentPath));
            if (versionDirectories != null)
            {
                getVisitedRootPaths().put(parentPath, versionDirectories);

                if (logger.isDebugEnabled())
                {
                    for (File directory : versionDirectories)
                    {
                        // We're using System.out.println() here for clarity and due to the length of the lines
                        System.out.println(" " + directory.getAbsolutePath());
                    }
                }

                String artifactPath = parentPath.substring(getRepository().getBasedir()
                                                                          .length() + 1, parentPath.length());

                try
                {
                    nugetChecksumManager.generateChecksum(getRepository(), artifactPath, versionDirectories,
                                                          forceRegeneration);
                }
                catch (IOException |
                               NoSuchAlgorithmException |
                               ProviderImplementationException |
                               UnknownRepositoryTypeException |
                               ArtifactTransportException e)
                {
                    logger.error("Failed to generate checksum for " + artifactPath, e);
                }
            }
        }
    }

    public LayoutProviderRegistry getLayoutProviderRegistry()
    {
        return layoutProviderRegistry;
    }

    public void setLayoutProviderRegistry(LayoutProviderRegistry layoutProviderRegistry)
    {
        this.layoutProviderRegistry = layoutProviderRegistry;
    }

    public NugetChecksumManager getNugetChecksumManager()
    {
        return nugetChecksumManager;
    }

    public void setNugetChecksumManager(NugetChecksumManager nugetChecksumManager)
    {
        this.nugetChecksumManager = nugetChecksumManager;
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
