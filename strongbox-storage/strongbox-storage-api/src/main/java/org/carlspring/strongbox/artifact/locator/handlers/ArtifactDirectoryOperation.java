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

    void execute(RepositoryPath path) throws IOException;

    LinkedHashMap<RepositoryPath, List<RepositoryPath>> getVisitedRootPaths();

    RepositoryPath getBasePath();
    
}
