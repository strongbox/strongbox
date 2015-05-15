package org.carlspring.strongbox.artifact.locator.handlers;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author mtodorov
 * @author stodorov
 */
public interface ArtifactDirectoryOperation
{

    void execute(Path path);

    LinkedHashMap<String, List<File>> getVisitedRootPaths();

    Storage getStorage();

    Repository getRepository();

    String getBasePath();

}
