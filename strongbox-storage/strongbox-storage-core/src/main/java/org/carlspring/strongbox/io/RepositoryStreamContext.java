package org.carlspring.strongbox.io;

import java.nio.file.Path;
import java.util.concurrent.locks.Lock;

public class RepositoryStreamContext
{

    private Path path;

    private Lock lock;

    private boolean opened;

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

}
