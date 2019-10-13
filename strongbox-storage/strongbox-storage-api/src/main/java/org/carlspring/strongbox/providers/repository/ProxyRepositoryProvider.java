package org.carlspring.strongbox.providers.repository;


import org.carlspring.strongbox.data.criteria.Paginator;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.providers.io.AbstractRepositoryProvider;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathLock;
import org.carlspring.strongbox.providers.repository.event.ProxyRepositoryPathExpiredEvent;
import org.carlspring.strongbox.providers.repository.event.RemoteRepositorySearchEvent;
import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 * @author Przemyslaw Fusik
 */
@Component
public class ProxyRepositoryProvider
        extends AbstractRepositoryProvider
{

    private static final Logger logger = LoggerFactory.getLogger(ProxyRepositoryProvider.class);

    private static final String ALIAS = "proxy";

    @Inject
    private ProxyRepositoryArtifactResolver proxyRepositoryArtifactResolver;

    @Inject
    private HostedRepositoryProvider hostedRepositoryProvider;

    @Inject
    private RepositoryPathLock repositoryPathLock;

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    protected InputStream getInputStreamInternal(RepositoryPath path)
        throws IOException
    {
        return hostedRepositoryProvider.getInputStreamInternal(path);
    }

    @Override
    protected RepositoryPath fetchPath(RepositoryPath repositoryPath)
        throws IOException
    {
        RepositoryPath targetPath = hostedRepositoryProvider.fetchPath(repositoryPath);

        if (targetPath == null)
        {
            targetPath = resolvePathExclusive(repositoryPath);
        }
        else if (RepositoryFiles.hasExpired(targetPath))
        {
            eventPublisher.publishEvent(new ProxyRepositoryPathExpiredEvent(targetPath));
        }

        return targetPath;
    }

    private RepositoryPath resolvePathExclusive(RepositoryPath repositoryPath)
            throws IOException
    {

        ReadWriteLock lockSource = repositoryPathLock.lock(repositoryPath, "pre-remote-fetch");
        Lock lock = lockSource.writeLock();
        lock.lock();

        try
        {
            // This is the second attempt, but this time inside exclusive write lock
            // Things might have changed.
            RepositoryPath targetPath = hostedRepositoryProvider.fetchPath(repositoryPath);
            if (targetPath != null)
            {
                return targetPath;

            }
            return proxyRepositoryArtifactResolver.fetchRemoteResource(repositoryPath);
        }
        catch (IOException e)
        {
            logger.error("Failed to resolve Path for proxied artifact [{}]",
                         repositoryPath, e);

            throw e;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    protected OutputStream getOutputStreamInternal(RepositoryPath repositoryPath)
            throws IOException
    {
        return Files.newOutputStream(repositoryPath);
    }

    @Override
    public List<Path> search(String storageId,
                             String repositoryId,
                             Predicate predicate,
                             Paginator paginator)
    {
        RemoteRepositorySearchEvent event = new RemoteRepositorySearchEvent(storageId,
                                                                            repositoryId,
                                                                            predicate,
                                                                            paginator);
        eventPublisher.publishEvent(event);

        return hostedRepositoryProvider.search(storageId, repositoryId, predicate, paginator);
    }

    @Override
    public Long count(String storageId,
                      String repositoryId,
                      Predicate predicate)
    {
        return hostedRepositoryProvider.count(storageId, repositoryId, predicate);
    }

    @Override
    protected ArtifactEntry provideArtifactEntry(RepositoryPath repositoryPath) throws IOException
    {
        ArtifactEntry artifactEntry = super.provideArtifactEntry(repositoryPath);
        ArtifactEntry remoteArtifactEntry = artifactEntry.getObjectId() == null ? new RemoteArtifactEntry() : (RemoteArtifactEntry) artifactEntry;

        return remoteArtifactEntry;
    }

    @Override
    protected boolean shouldStoreArtifactEntry(ArtifactEntry artifactEntry)
    {
        RemoteArtifactEntry remoteArtifactEntry = (RemoteArtifactEntry) artifactEntry;
        boolean result = super.shouldStoreArtifactEntry(artifactEntry) || !remoteArtifactEntry.getIsCached();

        remoteArtifactEntry.setIsCached(true);

        return result;
    }

}
