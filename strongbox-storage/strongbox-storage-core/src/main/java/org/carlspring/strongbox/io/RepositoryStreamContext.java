package org.carlspring.strongbox.io;

import java.nio.file.Path;
import java.util.concurrent.locks.Lock;

import org.springframework.transaction.TransactionStatus;

public class RepositoryStreamContext
{

    private Path path;

    private Lock lock;

    private boolean opened;

    private TransactionStatus transaction;

    private Boolean artifactUpdate;

    public Path getPath()
    {
        return path;
    }

    public void setPath(Path path)
    {
        this.path = path;
    }

    public Lock getLock()
    {
        return lock;
    }

    public void setLock(Lock lock)
    {
        this.lock = lock;
    }

    public boolean isOpened()
    {
        return opened;
    }

    public void setOpened(boolean opened)
    {
        this.opened = opened;
    }

    public TransactionStatus getTransaction()
    {
        return transaction;
    }

    public void setTransaction(TransactionStatus transaction)
    {
        this.transaction = transaction;
    }
    
    public Boolean getArtifactUpdate()
    {
        return artifactUpdate;
    }

    public void setArtifactUpdate(Boolean artifactUpdate)
    {
        this.artifactUpdate = artifactUpdate;
    }

}
