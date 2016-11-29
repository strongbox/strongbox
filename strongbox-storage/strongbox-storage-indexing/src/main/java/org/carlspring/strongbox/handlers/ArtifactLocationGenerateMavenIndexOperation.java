package org.carlspring.strongbox.handlers;

import org.carlspring.maven.commons.io.filters.PomFilenameFilter;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.locator.handlers.AbstractArtifactLocationHandler;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Kate Novik.
 */
@Component("mavenIndexOperation")
public class ArtifactLocationGenerateMavenIndexOperation
        extends AbstractArtifactLocationHandler
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactLocationGenerateMavenIndexOperation.class);

    private String previousPath;

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;

    public ArtifactLocationGenerateMavenIndexOperation()
    {
    }

    @Override
    public void execute(Path path)
    {
        File f = path.toAbsolutePath().toFile();

        String[] list = f.list(new PomFilenameFilter());
        List<String> filePaths = list != null ? Arrays.asList(list) : new ArrayList<>();

        String parentPath = path.getParent().toAbsolutePath().toString();

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

                String artifactPath = parentPath.substring(getRepository().getBasedir().length() + 1,
                                                           parentPath.length());

                String repositoryId = getRepository().getId();
                String storageId = getStorage().getId();

                RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndex(
                        storageId.concat(":").concat(repositoryId));

                File artifactFile = new File(repositoryId, artifactPath);
                Artifact artifact = ArtifactUtils.convertPathToArtifact(artifactPath);

                try
                {
                    indexer.addArtifactToIndex(repositoryId, artifactFile, artifact);
                }
                catch (IOException e)
                {
                    logger.error("Failed to add artifact to index " + artifactPath, e);
                }
            }
        }
    }
}
