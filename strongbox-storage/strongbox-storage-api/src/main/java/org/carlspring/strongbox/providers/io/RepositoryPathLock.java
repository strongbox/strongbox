package org.carlspring.strongbox.providers.io;

import javax.inject.Inject;
import java.net.URI;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.springframework.stereotype.Component;

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
    public void lock(RepositoryPath repositoryPath)
    {
        final IMap mapLocks = hazelcastInstance.getMap(REPOSITORY_PATH_MAP_LOCKS);
        final URI lock = repositoryPath.toUri();
        // DEV NOTE: map may be empty for locking
        // we don't have to put values to the map
        mapLocks.lock(lock);
    }

    public void unlock(RepositoryPath repositoryPath)
    {
        final IMap mapLocks = hazelcastInstance.getMap(REPOSITORY_PATH_MAP_LOCKS);
        final URI lock = repositoryPath.toUri();
        // The IMap-based locks are auto-destructed.
        mapLocks.unlock(lock);
    }
}
