package org.carlspring.strongbox.providers.io;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.hazelcast.core.HazelcastInstance;

import ca.thoughtwire.lock.DistributedLockService;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RepositoryPathLock
{

    private DistributedLockService lockService;

    @Inject
    public void setHazelcastInstance(HazelcastInstance hazelcastInstance)
    {
        lockService = DistributedLockService.newHazelcastLockService(hazelcastInstance);
    }

    public ReadWriteLock lock(final @Nonnull RepositoryPath repositoryPath)
    {
        return lock(repositoryPath, null);
    }
    
    public ReadWriteLock lock(final @Nonnull RepositoryPath repositoryPath, String id)
    {
        URI lock = getLock(repositoryPath);

        String lockName = Optional.ofNullable(id)
                                  .map(p -> String.format("%s?%s", lock, p))
                                  .orElseGet(() -> lock.toString());
        
        return lockService.getReentrantReadWriteLock(lockName);
    }

    private URI getLock(final @Nonnull RepositoryPath repositoryPath)
    {
        final URI lock = repositoryPath.toUri();
        
        Assert.isTrue(lock.isAbsolute(), String.format("Unable to lock relative path %s", lock));
        
        return lock;
    }
    
}
