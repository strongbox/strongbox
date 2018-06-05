package org.carlspring.strongbox.artifact.locator.handlers;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import org.carlspring.strongbox.providers.io.RepositoryPath;

/**
 * @author mtodorov
 * @author stodorov
 */
public interface ArtifactDirectoryOperation
{

    /**
     * Operation logic which need to be performed on provided directory.
     * 
     * @param directoryPath
     * @throws IOException
     */
    void execute(RepositoryPath directoryPath) throws IOException;

    LinkedHashMap<RepositoryPath, List<RepositoryPath>> getVisitedRootPaths();

    RepositoryPath getBasePath();
    
}
