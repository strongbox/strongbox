package org.carlspring.strongbox.providers.io;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
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

    public ReadWriteLock lock(final @Nonnull RepositoryPath repositoryPath) throws IOException
    {
        return lock(repositoryPath, null);
    }

    public ReadWriteLock lock(final @Nonnull RepositoryPath repositoryPath,
                              String id) throws IOException
    {
        URI lock = getLock(repositoryPath);
        String lockName = Optional.ofNullable(id)
                                  .map(p -> String.format("%s?%s", lock, p))
                                  .orElseGet(() -> lock.toString());
        logger.debug("Get lock for [{}]", lock);
        
        return lockService.getReentrantReadWriteLock(lockName);
    }

    private URI getLock(final @Nonnull RepositoryPath repositoryPath) throws IOException
    {
        if (RepositoryFiles.isArtifact(repositoryPath))
        {
            ArtifactCoordinates c = RepositoryFiles.readCoordinates(repositoryPath);
            // We should lock all the RepositoryArtifactIdGroup because there can be
            // `ArtifactEntryServiceImpl.updateLastVersionTag()` operations
            // which affetcs on other artifacts from group.
            return URI.create(URLEncoder.encode(c.getId(), "UTF-8"));
        }

        final URI lock = repositoryPath.toUri();

        Assert.isTrue(lock.isAbsolute(), String.format("Unable to lock relative path %s", lock));

        return lock;
    }

}
