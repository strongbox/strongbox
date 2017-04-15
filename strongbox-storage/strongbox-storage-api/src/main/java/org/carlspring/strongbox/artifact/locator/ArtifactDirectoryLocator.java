package org.carlspring.strongbox.artifact.locator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.carlspring.strongbox.artifact.locator.handlers.ArtifactDirectoryOperation;
import org.carlspring.strongbox.io.RepositoryPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class ArtifactDirectoryLocator
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactDirectoryLocator.class);

    private ArtifactDirectoryOperation operation;

    /**
     * The basedir to start analyzing from. Define this only, if you need to pass null values for operation.storage and
     * operation.repository.
     */
    private RepositoryPath basedir;

    public void locateArtifactDirectories()
        throws IOException
    {
        long startTime = System.currentTimeMillis();

        Files.walk(getStartingPath())
             .filter(Files::isDirectory)
             // Skip directories which start with a dot (like, for example: .index)
             .filter(path -> !path.getFileName().startsWith("."))
             // Note: Sorting can be expensive:
             .sorted()
             .forEach(this::execute);

        long endTime = System.currentTimeMillis();

        logger.debug("Executed (cache: " + operation.getVisitedRootPaths().size() + ")" +
                " visits in " + (endTime - startTime) + " ms.");

        getOperation().getVisitedRootPaths().clear();
    }

    public RepositoryPath getStartingPath()
    {
        // The root path
        RepositoryPath rootPath = basedir != null ? basedir : getOperation().getFileSystem().getRootDirectory();

        rootPath = rootPath.resolve(operation.getBasePath());
        rootPath = rootPath.normalize();

        logger.debug(String.format("ArtifactDirectoryLocator started in: path-[%s]", rootPath));

        return rootPath;
    }

    public ArtifactDirectoryOperation getOperation()
    {
        return operation;
    }

    public void setOperation(ArtifactDirectoryOperation operation)
    {
        this.operation = operation;
    }

    public RepositoryPath getBasedir()
    {
        return basedir;
    }

    public void setBasedir(RepositoryPath basedir)
    {
        this.basedir = basedir;
    }

    void execute(Path path){
        operation.execute((RepositoryPath) path);
    }
}
