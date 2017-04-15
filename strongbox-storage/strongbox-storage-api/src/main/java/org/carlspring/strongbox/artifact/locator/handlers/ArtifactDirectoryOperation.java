package org.carlspring.strongbox.artifact.locator.handlers;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import org.carlspring.strongbox.io.RepositoryFileSystem;
import org.carlspring.strongbox.io.RepositoryPath;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * @author mtodorov
 * @author stodorov
 */
public interface ArtifactDirectoryOperation
{

    void execute(RepositoryPath path) throws IOException;

    LinkedHashMap<RepositoryPath, List<RepositoryPath>> getVisitedRootPaths();

    Storage getStorage();

    Repository getRepository();

    RepositoryPath getBasePath();
    
    RepositoryFileSystem getFileSystem();

}
