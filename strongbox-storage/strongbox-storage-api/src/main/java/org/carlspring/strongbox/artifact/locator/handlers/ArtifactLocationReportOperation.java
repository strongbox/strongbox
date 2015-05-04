package org.carlspring.strongbox.artifact.locator.handlers;

import org.carlspring.maven.commons.io.filters.PomFilenameFilter;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * @author mtodorov
 */
public class ArtifactLocationReportOperation
        extends AbstractArtifactLocationHandler
{


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
        }
    }

}
