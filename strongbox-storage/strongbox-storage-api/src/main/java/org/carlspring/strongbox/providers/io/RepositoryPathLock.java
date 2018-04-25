package org.carlspring.strongbox.providers.io;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.net.URI;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RepositoryPathLock
{

    private static final String REPOSITORY_PATH_MAP_LOCKS = "strongbox-repository-path-map-locks";

    @Inject
    private HazelcastInstance hazelcastInstance;

    /**
     * @see <a href="http://docs.hazelcast.org/docs/latest-development/manual/html/Distributed_Data_Structures/Lock.html#page_ILock+vs.+IMap.lock">ILock vs. IMap.lock</a>
     */
    public void lock(final @Nonnull RepositoryPath repositoryPath)
    {
        final IMap mapLocks = hazelcastInstance.getMap(REPOSITORY_PATH_MAP_LOCKS);
        final URI lock = getLock(repositoryPath);
        // DEV NOTE: map may be empty for locking
        // we don't have to put values to the map
        mapLocks.lock(lock);
    }

    public void unlock(final @Nonnull RepositoryPath repositoryPath)
    {
        final IMap mapLocks = hazelcastInstance.getMap(REPOSITORY_PATH_MAP_LOCKS);
        final URI lock = getLock(repositoryPath);
        // The IMap-based locks are auto-destructed.
        mapLocks.unlock(lock);
    }

    private URI getLock(final @Nonnull RepositoryPath repositoryPath)
    {
        final URI lock = repositoryPath.toUri();
        Assert.isTrue(lock.isAbsolute(), String.format("Unable to lock relative path %s", lock));
        return lock;
    }
}
