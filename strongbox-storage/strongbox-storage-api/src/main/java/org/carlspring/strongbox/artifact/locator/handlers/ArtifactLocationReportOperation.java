package org.carlspring.strongbox.artifact.locator.handlers;

import org.carlspring.maven.commons.io.filters.PomFilenameFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author mtodorov
 * @author stodorov
 */
public class ArtifactLocationReportOperation
        extends AbstractArtifactLocationHandler
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactLocationReportOperation.class);

    private static String previousPath;

    private OutputStream outputStream = System.out;


    public ArtifactLocationReportOperation()
    {
    }

    public ArtifactLocationReportOperation(String basePath)
    {
        setBasePath(basePath);
    }

    public void execute(Path path)
    {
        File f = path.toAbsolutePath().toFile();
        List<String> filePaths = Arrays.asList(f.list(new PomFilenameFilter()));

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
                printPath(parentPath);
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

                System.out.println(path.getParent());
            }
        }
    }

    private void printPath(String parentPath)
    {
        try
        {
            synchronized (this)
            {
                outputStream.write(parentPath.getBytes());
                outputStream.write("\n".getBytes());
                outputStream.flush();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public OutputStream getOutputStream()
    {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream)
    {
        this.outputStream = outputStream;
    }

}
