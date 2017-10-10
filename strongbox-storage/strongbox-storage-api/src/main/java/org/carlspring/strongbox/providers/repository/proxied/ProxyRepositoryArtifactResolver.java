package org.carlspring.strongbox.providers.repository.proxied;

import org.carlspring.strongbox.client.ArtifactResolver;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessCacheManager;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;

/**
 * @author Przemyslaw Fusik
 */
public abstract class ProxyRepositoryArtifactResolver
{

    @Inject
    protected ConfigurationManager configurationManager;

    @Inject
    protected RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager;

    @Inject
    protected LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    protected ArtifactEventListenerRegistry artifactEventListenerRegistry;

    @Inject
    protected ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    public InputStream getInputStream(final String storageId,
                                      final String repositoryId,
                                      final String path)
            throws IOException, ArtifactTransportException, NoSuchAlgorithmException, ProviderImplementationException
    {
        final Storage storage = getConfiguration().getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);

        getLogger().debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        final InputStream candidate = preRemoteRepositoryAttempt(repository, path);
        if (candidate != null)
        {
            return candidate;
        }

        final RemoteRepository remoteRepository = repository.getRemoteRepository();
        if (!remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository))
        {
            getLogger().debug("Remote repository '" + remoteRepository.getUrl() + "' is down.");

            return null;
        }

        final ArtifactResolver client = new ArtifactResolver(proxyRepositoryConnectionPoolConfigurationService.getRestClient());
        client.setRepositoryBaseUrl(remoteRepository.getUrl());
        client.setUsername(remoteRepository.getUsername());
        client.setPassword(remoteRepository.getPassword());

        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final String resourcePath = layoutProvider.resolveResourcePath(repository, path);

        try (final CloseableProxyRepositoryResponse closeableProxyRepositoryResponse =
                     new CloseableProxyRepositoryResponse(client.getResourceWithResponse(resourcePath)))
        {
            final Response response = closeableProxyRepositoryResponse.response;

            if (response.getStatus() != 200 || response.getEntity() == null)
            {
                return null;
            }

            InputStream is = response.readEntity(InputStream.class);
            if (is == null)
            {
                return null;
            }

            is =  post(is, storageId, repositoryId, path);

            final RepositoryPath artifactPath = layoutProvider.resolve(repository).resolve(path);
            if (!layoutProvider.isChecksum(artifactPath) && !layoutProvider.isMetadata(path))
            {
                artifactEventListenerRegistry.dispatchArtifactDownloadedEvent(storageId, repositoryId, path);
            }

            return is;


        }
    }

    protected InputStream post(final InputStream is,
                               final String storageId,
                               final String repositoryId,
                               final String path)
            throws IOException, NoSuchAlgorithmException, ProviderImplementationException
    {
        return is;
    }

    protected InputStream preRemoteRepositoryAttempt(Repository repository,
                                                     String path)
            throws IOException
    {
        return null;
    }

    protected abstract Logger getLogger();

    private Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    private class CloseableProxyRepositoryResponse
            implements Closeable
    {

        private final Response response;

        private CloseableProxyRepositoryResponse(Response response)
        {
            this.response = response;
        }

        @Override
        public void close()
                throws IOException
        {
            response.close();
        }
    }

}
