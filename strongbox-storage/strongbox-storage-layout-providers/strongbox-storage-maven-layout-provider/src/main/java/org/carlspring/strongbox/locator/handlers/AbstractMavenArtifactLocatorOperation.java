package org.carlspring.strongbox.locator.handlers;

import org.carlspring.maven.commons.io.filters.PomFilenameFilter;
import org.carlspring.strongbox.artifact.locator.handlers.AbstractArtifactLocationHandler;
import org.carlspring.strongbox.io.RepositoryPath;
import org.carlspring.strongbox.storage.metadata.VersionCollectionRequest;
import org.carlspring.strongbox.storage.metadata.VersionCollector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    public void execute(RepositoryPath path) throws IOException
    {
//        File f = path.toAbsolutePath().toFile();

        List<Path> filePathList = Files.walk(path)
                                       .filter(p -> !p.getFileName().startsWith(".pom"))
                                       .sorted()
                                       .collect(Collectors.toList());
        
//        String[] list = f.list(new PomFilenameFilter());
//        List<String> filePaths = list != null ? Arrays.asList(list) : new ArrayList<>();

        RepositoryPath parentPath = path.getParent();

        if (filePathList.isEmpty()){
            return;
        }
        
        // Don't enter visited paths (i.e. version directories such as 1.2, 1.3, 1.4...)
        if (getVisitedRootPaths().containsKey(parentPath) && getVisitedRootPaths().get(parentPath).contains(path))
        {
            return;
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
        if (versionDirectories != null)
        {
            return;
        }
        getVisitedRootPaths().put(parentPath, versionDirectories);

        VersionCollector versionCollector = new VersionCollector();
        VersionCollectionRequest request = versionCollector.collectVersions(path.getParent().toAbsolutePath());

        if (logger.isDebugEnabled())
        {
            for (RepositoryPath directory : versionDirectories)
            {
                // We're using System.out.println() here for clarity and due to the length of the lines
                System.out.println(" " + directory.toAbsolutePath());
            }
        }

        RepositoryPath artifactPath = (RepositoryPath) parentPath.relativize(getFileSystem().getRootDirectory());
        
        //String artifactPath = parentPath.substring(getRepository().getBasedir().length() + 1, parentPath.length());

        executeOperation(request, artifactPath, versionDirectories);
    }

    public abstract void executeOperation(VersionCollectionRequest request,
                                          RepositoryPath artifactPath,
                                          List<RepositoryPath> versionDirectories);

}
