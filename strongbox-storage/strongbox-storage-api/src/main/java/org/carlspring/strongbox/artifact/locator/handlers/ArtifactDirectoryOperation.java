package org.carlspring.strongbox.artifact.locator.handlers;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.nio.file.Path;
import java.util.List;

/**
 * @author mtodorov
 */
public interface ArtifactDirectoryOperation
{

    void execute(Path path);

    List<String> getVisitedRootPaths();

    Storage getStorage();

    Repository getRepository();

    String getBasePath();

}
