package org.carlspring.strongbox.providers.repository.proxied;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import javax.inject.Inject;

import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathLock;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.providers.io.TempRepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.repository.HostedRepositoryProvider;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessCacheManager;
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
    protected ConfigurationManager configurationManager;

    @Inject
    protected RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager;

    @Inject
    protected LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;

    @Inject
    protected RestArtifactResolverFactory restArtifactResolverFactory;
    
    @Inject
    protected RepositoryPathResolver repositoryPathResolver;
    
    @Inject
    protected RepositoryPathLock repositoryPathLock;
    
    @Inject
    private HostedRepositoryProvider hostedRepositoryProvider;

    @Inject
    private ArtifactManagementService artifactManagementService;

    public RepositoryPath fetchRemoteResource(RepositoryPath repositoryPath)
        throws IOException
    {
        Repository repository = repositoryPath.getFileSystem().getRepository();
        logger.debug(String.format("Checking in [%s]...", repositoryPath));

        final RepositoryPath candidate = preProxyRepositoryAccessAttempt(repositoryPath);
        if (candidate != null)
        {
            return candidate;
        }

        final RemoteRepository remoteRepository = repository.getRemoteRepository();
        if (!remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository))
        {
            logger.debug("Remote repository '" + remoteRepository.getUrl() + "' is down.");

            return null;
        }

        RestArtifactResolver client = restArtifactResolverFactory.newInstance(remoteRepository);
        
        repositoryPathLock.lock(repositoryPath);
        try (InputStream is = new BufferedInputStream(new ProxyRepositoryInputStream(client, repositoryPath)))
        {
            if (RepositoryFiles.artifactExists(repositoryPath))
            {
                return repositoryPath;
            }
            
            return doFetch(repositoryPath, is);
        } 
        finally
        {
            repositoryPathLock.unlock(repositoryPath);
        }
    }

    private RepositoryPath doFetch(RepositoryPath repositoryPath,
                                   InputStream is)
        throws IOException
    {
        //We need this to force initialize lazy connection to remote repository.
        int available = is.available();
        logger.debug(String.format("Got [%s] avaliable bytes for [%s].", available, repositoryPath));
        
        
        RepositoryPath result = onSuccessfulProxyRepositoryResponse(is, repositoryPath);
        
        RepositoryFileAttributes artifactFileAttributes = Files.readAttributes(repositoryPath,
                                                                               RepositoryFileAttributes.class);
        if (!artifactFileAttributes.isArtifact())
        {
            return result;
        }
        
        artifactEventListenerRegistry.dispatchArtifactFetchedFromRemoteEvent(result);
        
        return result;
    }

    protected RepositoryPath preProxyRepositoryAccessAttempt(RepositoryPath repositoryPath)
            throws IOException
    {
        return hostedRepositoryProvider.fetchPath(repositoryPath);
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

    protected Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
