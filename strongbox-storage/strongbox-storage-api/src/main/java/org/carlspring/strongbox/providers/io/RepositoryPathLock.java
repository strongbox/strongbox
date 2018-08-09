package org.carlspring.strongbox.providers.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.io.input.ProxyInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(RepositoryPathLock.class);

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

    public ReadWriteLock lock(final @Nonnull RepositoryPath repositoryPath,
                              String id)
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

    public InputStream lockInputStream(RepositoryPath repositoryPath,
                                       StreamSupplier<? extends InputStream> streamSuplier)
        throws IOException
    {
        Lock lock = lock(repositoryPath).readLock();
        try
        {
            lock.lock();
            return newInputStream(lock, streamSuplier);
        }
        catch (Exception e)
        {
            unlock(lock);
            throw e instanceof IOException ? (IOException) e : new IOException(e);
        }

    }

    private void unlock(Lock lock)
    {
        if (lock == null)
        {
            return;
        }
        try
        {
            lock.unlock();
        }
        catch (Exception e)
        {
            logger.error(String.format("Failed to unlock [%s].", RepositoryPath.class.getSimpleName()), e);
        }
    }

    private InputStream newInputStream(Lock lock,
                                       StreamSupplier<? extends InputStream> streamSuplier)
        throws IOException
    {
        InputStream is = streamSuplier.get();
        return new ProxyInputStream(is)
        {

            @Override
            public void close()
                throws IOException
            {
                try
                {
                    super.close();
                } finally
                {
                    unlock(lock);
                }
            }

        };
    }

    @FunctionalInterface
    public interface StreamSupplier<T>
    {

        T get()
            throws IOException;

    }

}
