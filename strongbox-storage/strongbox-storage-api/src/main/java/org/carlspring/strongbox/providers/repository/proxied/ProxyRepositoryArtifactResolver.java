package org.carlspring.strongbox.providers.repository.proxied;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.inject.Inject;

import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathLock;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ProxyRepositoryArtifactResolver
{
    private static final Logger logger = LoggerFactory.getLogger(ProxyRepositoryArtifactResolver.class);

    @Inject
    private RemoteRepositoryAlivenessService remoteRepositoryAlivenessCacheManager;

    @Inject
    private ArtifactEventListenerRegistry artifactEventListenerRegistry;

    @Inject
    private RestArtifactResolverFactory restArtifactResolverFactory;
    
    @Inject
    private RepositoryPathLock repositoryPathLock;

    @Inject
    private ArtifactManagementService artifactManagementService;

    /**
     * This method has been developed to force fetch resource from remote.
     *
     * It should not contain any local / cache existence checks.
     *
     * Update this method carefully.
     */
    public RepositoryPath fetchRemoteResource(RepositoryPath repositoryPath)
        throws IOException
    {
        Repository repository = repositoryPath.getFileSystem().getRepository();
        final RemoteRepository remoteRepository = repository.getRemoteRepository();
        if (!remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository))
        {
            logger.debug("Remote repository '{}' is down.", remoteRepository.getUrl());

            return null;
        }

        RestArtifactResolver client = restArtifactResolverFactory.newInstance(remoteRepository);

        ReadWriteLock lockSource = repositoryPathLock.lock(repositoryPath, "remote-fetch");
        Lock lock = lockSource.writeLock();
        lock.lock();

        try (InputStream is = new BufferedInputStream(new ProxyRepositoryInputStream(client, repositoryPath)))
        {
            return doFetch(repositoryPath, is);
        }
        finally
        {
            lock.unlock();
        }
    }

    private RepositoryPath doFetch(RepositoryPath repositoryPath,
                                   InputStream is)
        throws IOException
    {
        //We need this to force initialize lazy connection to remote repository.
        int available = is.available();
        logger.debug("Got [{}] available bytes for [{}].", available, repositoryPath);
        
        
        RepositoryPath result = onSuccessfulProxyRepositoryResponse(is, repositoryPath);
        
        RepositoryFileAttributes artifactFileAttributes = Files.readAttributes(repositoryPath,
                                                                               RepositoryFileAttributes.class);
        if (artifactFileAttributes.isArtifact())
        {
            artifactEventListenerRegistry.dispatchArtifactFetchedFromRemoteEvent(result);
        }
        
        return result;
    }

    protected RepositoryPath onSuccessfulProxyRepositoryResponse(InputStream is,
                                                                 RepositoryPath repositoryPath)
            throws IOException
    {
         artifactManagementService.store(repositoryPath, is);
         
        // TODO: Add a policy for validating the checksums of downloaded artifacts
        // TODO: Validate the local checksum against the remote's checksums
        // sbespalov: we have checksum validation within ArtifactManagementService.store() method, but it's not strict for now (see SB-949)
        
        // Serve the downloaded artifact
        return repositoryPath;
    }

}
