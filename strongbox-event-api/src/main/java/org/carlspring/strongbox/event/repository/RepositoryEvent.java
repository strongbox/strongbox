package org.carlspring.strongbox.event.repository;

import org.carlspring.strongbox.event.RepositoryBasedEvent;

/**
 * @author mtodorov
 */
public class RepositoryEvent extends RepositoryBasedEvent
{

    public RepositoryEvent(String storageId,
                           String repositoryId,
                           String path,
                           int type)
    {
        setStorageId(storageId);
        setRepositoryId(repositoryId);
        setPath(path);
        setType(type);
    }

    public RepositoryEvent(String storageId,
                           String repositoryId,
                           int type)
    {
        setType(type);
        setStorageId(storageId);
        setRepositoryId(repositoryId);
    }

}
