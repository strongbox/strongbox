package org.carlspring.strongbox.artifact;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.locks.ReadWriteLock;

import javax.inject.Inject;

import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.event.AsyncEventListener;
import org.carlspring.strongbox.event.artifact.ArtifactEvent;
import org.carlspring.strongbox.event.artifact.ArtifactEventTypeEnum;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathLock;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class AsyncArtifactEntryHandler
{
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncArtifactEntryHandler.class);

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private RepositoryPathLock repositoryPathLock;

    @Inject
    private PlatformTransactionManager transactionManager;

    private final ArtifactEventTypeEnum eventType;

    public AsyncArtifactEntryHandler(ArtifactEventTypeEnum eventType)
    {
        super();
        this.eventType = eventType;
    }

    @AsyncEventListener
    public void handleEvent(final ArtifactEvent<RepositoryPath> event)
        throws IOException,
        InterruptedException
    {
        if (eventType.getType() != event.getType())
        {
            return;
        }

        RepositoryPath repositoryPath = (RepositoryPath) event.getPath();
        if (!RepositoryFiles.isArtifact(repositoryPath))
        {
            return;
        }

        // TODO: please don't panic, this is needed just as workadound to have
        // new transaction within this async event (expected to be replaced with
        // just Propagation.REQUIRES_NEW after SB-1200)
        Object sync = new Object();
        new Thread(() -> {
            try
            {
                handleLocked(repositoryPath);
            }
            catch (Exception e)
            {
                logger.error(String.format("Failed to handle async event [%s]",
                                           AsyncArtifactEntryHandler.this.getClass().getSimpleName()),
                             e);
            } 
            finally
            {
                synchronized (sync)
                {
                    sync.notifyAll();
                }
            }
        }).start();

        synchronized (sync)
        {
            sync.wait();
        }
    }

    private void handleLocked(RepositoryPath repositoryPath)
    {
        ReadWriteLock lock = repositoryPathLock.lock(repositoryPath,
                                                     ArtifactEntry.class.getSimpleName());
        lock.writeLock().lock();
        try
        {
            handleTransactional(repositoryPath);
        } finally
        {
            lock.writeLock().unlock();
        }
    }

    private void handleTransactional(RepositoryPath repositoryPath)
    {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.execute(t -> {
            try
            {
                return artifactEntryService.save(handleEvent(repositoryPath));
            }
            catch (IOException e)
            {
                throw new UndeclaredThrowableException(e);
            }
        });
    }

    protected abstract ArtifactEntry handleEvent(RepositoryPath repositoryPath)
        throws IOException;

}
