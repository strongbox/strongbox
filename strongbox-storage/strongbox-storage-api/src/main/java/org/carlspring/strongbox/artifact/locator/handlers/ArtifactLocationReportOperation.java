package org.carlspring.strongbox.artifact.locator.handlers;

import org.carlspring.maven.commons.io.filters.PomFilenameFilter;
import org.carlspring.strongbox.io.RepositoryPath;

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
 * @author stodorov
 */
public class ArtifactLocationReportOperation
        extends AbstractArtifactLocationHandler
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactLocationReportOperation.class);

    private RepositoryPath previousPath;


    public ArtifactLocationReportOperation()
    {
    }

    public ArtifactLocationReportOperation(RepositoryPath basePath)
    {
        setBasePath(basePath);
    }

    public void execute(RepositoryPath path) throws IOException
    {
        //File f = path.toAbsolutePath().toFile();

        //String[] list = f.list(new PomFilenameFilter());
        //List<String> filePaths = list != null ? Arrays.asList(list) : new ArrayList<>();

        List<Path> filePathList = Files.walk(path)
                .filter(p -> !p.getFileName().startsWith(".pom"))
                .sorted()
                .collect(Collectors.toList());
        
        RepositoryPath parentPath = path.getParent().toAbsolutePath();

        if (filePathList.isEmpty())
        {
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
            getVisitedRootPaths().put(parentPath, versionDirectories);

            System.out.println(path.getParent());
        }
    }

}
