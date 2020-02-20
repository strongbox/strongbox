package org.carlspring.strongbox.artifact;

import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.domain.ArtifactEntity;
import org.carlspring.strongbox.event.AsyncEventListener;
import org.carlspring.strongbox.event.artifact.ArtifactEvent;
import org.carlspring.strongbox.event.artifact.ArtifactEventTypeEnum;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathLock;
import org.carlspring.strongbox.services.ArtifactEntryService;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.locks.Lock;

import com.orientechnologies.common.concur.ONeedRetryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class AsyncArtifactEntryHandler
{

    private static final int MAX_RETRY = 10;

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

        // TODO: this is needed just as workadound to have new transaction
        // within this async event (expected to be replaced with
        // just Propagation.REQUIRES_NEW after SB-1200)
        Thread threadWithNewTransactionContext = new Thread(() -> {
            try
            {
                handleLocked(repositoryPath);
            }
            catch (Exception e)
            {
                logger.error("Failed to handle async event [{}]",
                             AsyncArtifactEntryHandler.this.getClass().getSimpleName(),
                             e);
            }
        });

        threadWithNewTransactionContext.start();
        threadWithNewTransactionContext.join();
    }

    private void handleLocked(RepositoryPath repositoryPath)
        throws IOException,
        InterruptedException
    {
        Lock lock = repositoryPathLock.lock(repositoryPath,
                                            ArtifactEntity.class.getSimpleName())
                                      .writeLock();
        lock.lock();
        try
        {
            handleTransactional(repositoryPath);
        } 
        finally
        {
            lock.unlock();
        }
    }

    private void handleTransactional(RepositoryPath repositoryPath)
    {
        new TransactionTemplate(transactionManager).execute(t -> {
            try
            {
                Artifact result = handleEvent(repositoryPath);
                if (result == null)
                {
                    logger.debug("No [{}] result for event [{}] and path [{}].",
                                 ArtifactEntity.class.getSimpleName(),
                                 AsyncArtifactEntryHandler.this.getClass().getSimpleName(),
                                 repositoryPath);

                    return null;
                }

                return artifactEntryService.save(result);
            }
            catch (IOException e)
            {
                throw new UndeclaredThrowableException(e);
            }
        });
    }

    protected abstract Artifact handleEvent(RepositoryPath repositoryPath)
        throws IOException;

}
