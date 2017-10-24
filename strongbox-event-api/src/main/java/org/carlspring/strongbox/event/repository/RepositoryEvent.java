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
        super(storageId, repositoryId, type);
        setPath(path);
    }

    public RepositoryEvent(String storageId,
                           String repositoryId,
                           int type)
    {
        super(storageId, repositoryId, type);
    }

}
