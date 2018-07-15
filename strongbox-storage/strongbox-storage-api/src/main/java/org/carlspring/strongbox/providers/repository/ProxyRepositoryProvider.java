package org.carlspring.strongbox.providers.repository;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.carlspring.strongbox.data.criteria.Paginator;
import org.carlspring.strongbox.data.criteria.Predicate;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.providers.io.AbstractRepositoryProvider;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.repository.event.RemoteRepositorySearchEvent;
import org.carlspring.strongbox.providers.repository.proxied.ProxyRepositoryArtifactResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
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
    private ApplicationEventPublisher eventPublisher;


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
            targetPath = resolvePathForceFetch(repositoryPath);
        }
        
        return targetPath;
    }

    public RepositoryPath resolvePathForceFetch(RepositoryPath repositoryPath)
        throws IOException
    {
        try
        {
            return (RepositoryPath) proxyRepositoryArtifactResolver.fetchRemoteResource(repositoryPath);
        }
        catch (IOException e)
        {
            logger.error(String.format("Failed to resolve Path for proxied artifact [%s]", repositoryPath),
                         e);

            throw e;
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
        RemoteRepositorySearchEvent event = new RemoteRepositorySearchEvent(storageId, repositoryId, predicate, null);
        eventPublisher.publishEvent(event);

        return hostedRepositoryProvider.count(storageId, repositoryId, predicate);
    }

    @Override
    protected ArtifactEntry provideArtifactEntry(RepositoryPath repositoryPath, boolean create, boolean lock) throws IOException
    {
        ArtifactEntry artifactEntry = super.provideArtifactEntry(repositoryPath, create, lock);
        
        if (artifactEntry instanceof RemoteArtifactEntry)
        {
            ((RemoteArtifactEntry) artifactEntry).setIsCached(true);
            return artifactEntry;
        }
        else if (artifactEntry == null)
        {
            return null;
        }

        return artifactEntry.getObjectId() == null ? createCachedRemoteArtifactEntry()
                : (RemoteArtifactEntry) artifactEntry;
    }

    private RemoteArtifactEntry createCachedRemoteArtifactEntry()
    {
        RemoteArtifactEntry result = new RemoteArtifactEntry();
        result.setIsCached(Boolean.TRUE);
        
        return result;
    }

}
