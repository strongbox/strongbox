package org.carlspring.strongbox.artifact.locator;

import org.carlspring.strongbox.artifact.locator.handlers.ArtifactDirectoryOperation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import static org.carlspring.strongbox.util.FileUtils.normalizePath;

/**
 * @author mtodorov
 */
public class ArtifactDirectoryLocator
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactDirectoryLocator.class);

    private ArtifactDirectoryOperation operation;

    /**
     * The basedir to start analyzing from. Define this only,
     * if you need to pass null values for operation.storage and operation.repository.
     */
    private String basedir;


    public void locateArtifactDirectories()
            throws IOException
    {
        long startTime = System.currentTimeMillis();

        Files.walk(Paths.get(getStartingPath()))
             .filter(Files::isDirectory)
             // Skip directories which start with a dot (like, for example: .index)
             .filter(path -> !path.getFileName().startsWith("."))
             // Note: Sorting can be expensive:
             .sorted()
             .forEach(operation::execute);

        long endTime = System.currentTimeMillis();

        logger.debug("Executed (cache: " + operation.getVisitedRootPaths().size() + ")" +
                     " visits in " + (endTime - startTime) + " ms.");

        getOperation().getVisitedRootPaths().clear();
    }

    public String getStartingPath()
    {
        // The root path
        String rootPath = basedir != null ? basedir : getOperation().getRepository().getBasedir();
        rootPath = normalizePath(rootPath);

        // The base path to be appended to the root path
        String basePath = getOperation().getBasePath() != null ? getOperation().getBasePath() : "";
        if (!StringUtils.isEmpty(basePath) && !basePath.startsWith("/"))
        {
            basePath = "/" + basePath;
        }

        basePath = normalizePath(basePath);

        System.out.println("basePath = " + rootPath + basePath);

        return rootPath + basePath;
    }

    public ArtifactDirectoryOperation getOperation()
    {
        return operation;
    }

    public void setOperation(ArtifactDirectoryOperation operation)
    {
        this.operation = operation;
    }

    public String getBasedir()
    {
        return basedir;
    }

    public void setBasedir(String basedir)
    {
        this.basedir = basedir;
    }

}
