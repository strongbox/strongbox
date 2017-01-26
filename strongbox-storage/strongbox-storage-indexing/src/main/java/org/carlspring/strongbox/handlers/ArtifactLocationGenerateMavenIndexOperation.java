package org.carlspring.strongbox.handlers;

import org.carlspring.maven.commons.io.filters.JarFilenameFilter;
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

/**
 * @author Kate Novik.
 */
public class ArtifactLocationGenerateMavenIndexOperation
        extends AbstractArtifactLocationHandler
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactLocationGenerateMavenIndexOperation.class);

    private String previousPath;

    private RepositoryIndexManager repositoryIndexManager;

    public ArtifactLocationGenerateMavenIndexOperation()
    {
    }

    public ArtifactLocationGenerateMavenIndexOperation(RepositoryIndexManager repositoryIndexManager)
    {
        this.repositoryIndexManager = repositoryIndexManager;
    }

    @Override
    public void execute(Path path)
    {
        if (path != null)
        {
            File f = path.toAbsolutePath()
                         .toFile();

            String[] list = f.list(new PomFilenameFilter());

            if (list != null)
            {
                String[] listJar = f.list(new JarFilenameFilter());
                List<String> filePaths = listJar != null ? Arrays.asList(listJar) : new ArrayList<>();

                String parentPath = path.getParent()
                                        .toAbsolutePath()
                                        .toString();

                if (!filePaths.isEmpty())
                {
                    //absolute path to artifact
                    String resultPath = Paths.get(f.getPath(), filePaths.get(0))
                                             .toString();

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
                        logger.debug(parentPath);
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
                                logger.debug(" " + directory.getAbsolutePath());
                            }
                        }

                        String artifactPath = resultPath.substring(getRepository().getBasedir()
                                                                                  .length() + 1,
                                                                   resultPath.length());

                        String repositoryId = getRepository().getId();
                        String storageId = getStorage().getId();

                        RepositoryIndexer indexer = repositoryIndexManager.getRepositoryIndex(
                                storageId.concat(":")
                                         .concat(repositoryId));

                        if (indexer != null)
                        {
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
        }
    }

    public RepositoryIndexManager getRepositoryIndexManager()
    {
        return repositoryIndexManager;
    }

    public void setRepositoryIndexManager(RepositoryIndexManager repositoryIndexManager)
    {
        this.repositoryIndexManager = repositoryIndexManager;
    }
}
