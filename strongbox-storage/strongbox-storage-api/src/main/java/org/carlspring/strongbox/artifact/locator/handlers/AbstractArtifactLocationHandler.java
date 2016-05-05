package org.carlspring.strongbox.artifact.locator.handlers;

import org.carlspring.strongbox.common.io.filters.ArtifactVersionDirectoryFilter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author mtodorov
 * @author stodorov
 */
public abstract class AbstractArtifactLocationHandler
    implements ArtifactDirectoryOperation
{

    private Storage storage;

    private Repository repository;

    private LinkedHashMap<String, List<File>> visitedRootPaths = new LinkedHashMap<>();

    /**
     * The base path within the repository from where to start scanning for artifacts.
     */
    private String basePath;


    public LinkedHashMap<String, List<File>> getVisitedRootPaths()
    {
        return visitedRootPaths;
    }

    public List<File> getVersionDirectories(Path basePath)
    {
        File basedir = basePath.toFile();
        File[] versionDirectories = basedir.listFiles(new ArtifactVersionDirectoryFilter());

        if (versionDirectories != null)
        {
            List<File> directories = Arrays.asList(versionDirectories);

            Collections.sort(directories);

            return directories;
        }
        else
        {
            return null;
        }
    }

    @Override
    public Storage getStorage()
    {
        return storage;
    }

    public void setStorage(Storage storage)
    {
        this.storage = storage;
    }

    @Override
    public Repository getRepository()
    {
        return repository;
    }

    public void setRepository(Repository repository)
    {
        this.repository = repository;
    }

    @Override
    public String getBasePath()
    {
        return basePath;
    }

    public void setBasePath(String basePath)
    {
        this.basePath = basePath;
    }

}
