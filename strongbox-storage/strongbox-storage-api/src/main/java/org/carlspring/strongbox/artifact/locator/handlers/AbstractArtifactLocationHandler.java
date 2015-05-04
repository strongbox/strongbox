package org.carlspring.strongbox.artifact.locator.handlers;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
public abstract class AbstractArtifactLocationHandler
    implements ArtifactDirectoryOperation
{

    private Storage storage;

    private Repository repository;

    private List<String> visitedRootPaths = new ArrayList<>();

    /**
     * The base path within the repository from where to start scanning for artifacts.
     */
    private String basePath;


    public List<String> getVisitedRootPaths()
    {
        return visitedRootPaths;
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
