package org.carlspring.strongbox.artifact;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.concurrent.locks.Lock;

import javax.inject.Inject;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.carlspring.strongbox.domain.Artifact;
import org.carlspring.strongbox.event.AsyncEventListener;
import org.carlspring.strongbox.event.artifact.ArtifactEvent;
import org.carlspring.strongbox.event.artifact.ArtifactEventTypeEnum;
import org.carlspring.strongbox.gremlin.dsl.EntityTraversalSource;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathLock;
import org.carlspring.strongbox.repositories.ArtifactRepository;
import org.janusgraph.core.JanusGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class AsyncArtifactEntryHandler
{

    private static final Logger logger = LoggerFactory.getLogger(AsyncArtifactEntryHandler.class);

    @Inject
    private ArtifactRepository artifactEntityRepository;
    @Inject
    private RepositoryPathLock repositoryPathLock;
    @Inject
    private PlatformTransactionManager transactionManager;
    @Inject
    private JanusGraph janusGraph;

    private final ArtifactEventTypeEnum eventType;

    public AsyncArtifactEntryHandler(ArtifactEventTypeEnum eventType)
    {
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
        try
        {
            handleLocked(repositoryPath);
        }
        catch (Throwable e)
        {
            logger.error("Failed to handle async event [{}]",
                         AsyncArtifactEntryHandler.this.getClass().getSimpleName(),
                         e);
        }
    }

    private void handleLocked(RepositoryPath repositoryPath)
        throws IOException,
        InterruptedException
    {
        Lock lock = repositoryPathLock.lock(repositoryPath,
                                            Artifact.class.getSimpleName())
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
        Graph g = janusGraph.tx().createThreadedTx();
        try
        {
            Artifact result = handleEvent(repositoryPath);
            if (result == null)
            {
                logger.debug("No [{}] result for event [{}] and path [{}].",
                             Artifact.class.getSimpleName(),
                             AsyncArtifactEntryHandler.this.getClass().getSimpleName(),
                             repositoryPath);

                return;
            }

            artifactEntityRepository.merge(() -> g.traversal(EntityTraversalSource.class), result);
            g.tx().commit();
        }
        catch (Throwable e)
        {
            g.tx().rollback();
            throw new UndeclaredThrowableException(e);
        }
        finally
        {
            g.tx().close();
        }
    }

    protected abstract Artifact handleEvent(RepositoryPath repositoryPath)
        throws IOException;

}
