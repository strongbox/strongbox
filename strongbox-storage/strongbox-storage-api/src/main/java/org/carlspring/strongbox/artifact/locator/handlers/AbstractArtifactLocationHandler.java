package org.carlspring.strongbox.artifact.locator.handlers;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import org.carlspring.strongbox.io.RepositoryFileSystem;
import org.carlspring.strongbox.io.RepositoryPath;
import org.carlspring.strongbox.io.filters.ArtifactVersionDirectoryFilter;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

/**
 * @author mtodorov
 * @author stodorov
 */
public abstract class AbstractArtifactLocationHandler
        implements ArtifactDirectoryOperation
{

    private Storage storage;
    private FilenameFilter filter;
    private LinkedHashMap<RepositoryPath, List<RepositoryPath>> visitedRootPaths = new LinkedHashMap<>();
    private RepositoryFileSystem repositoryFileSystem;

    /**
     * The base path within the repository from where to start scanning for artifacts.
     */
    private RepositoryPath basePath;


    public LinkedHashMap<RepositoryPath, List<RepositoryPath>> getVisitedRootPaths()
    {
        return visitedRootPaths;
    }

    public List<RepositoryPath> getVersionDirectories(Path basePath)
    {
        File basedir = basePath.toFile();
        File[] versionDirectories = basedir.listFiles(new ArtifactVersionDirectoryFilter(filter));

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
        return basePath.getFileSystem().getRepository();
    }

    @Override
    public RepositoryPath getBasePath()
    {
        return basePath;
    }

    public void setBasePath(RepositoryPath basePath)
    {
        this.basePath = basePath;
    }

    public FilenameFilter getFilenameFilter()
    {
        return filter;
    }

    public void setFilenameFilter(FilenameFilter filter)
    {
        this.filter = filter;
    }

    public RepositoryFileSystem getFileSystem()
    {
        return repositoryFileSystem;
    }

    public void setFileSystem(RepositoryFileSystem repositoryFileSystem)
    {
        this.repositoryFileSystem = repositoryFileSystem;
    }
    
}
