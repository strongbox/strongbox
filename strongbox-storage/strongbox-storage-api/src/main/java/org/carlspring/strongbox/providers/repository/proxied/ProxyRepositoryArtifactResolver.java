package org.carlspring.strongbox.providers.repository.proxied;

import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.client.CloseableRestResponse;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.client.RestArtifactResolverFactory;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.event.artifact.ArtifactEventListenerRegistry;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessCacheManager;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
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
    protected RestArtifactResolverFactory restArtifactResolverFactory;

    public InputStream getInputStream(final String storageId,
                                      final String repositoryId,
                                      final String path)
            throws IOException, ArtifactTransportException, NoSuchAlgorithmException, ProviderImplementationException
    {
        final Storage storage = getConfiguration().getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);

        getLogger().debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        final InputStream candidate = preProxyRepositoryAccessAttempt(repository, path);
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

        try (final RestArtifactResolver client = restArtifactResolverFactory.newInstance(remoteRepository.getUrl(),
                                                                                         remoteRepository.getUsername(),
                                                                                         remoteRepository.getPassword()))
        {
            final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());

            URI resource;
            try
            {
                resource = layoutProvider.resolveResource(repository, path);
            }
            catch (IllegalArgumentException e)
            {
                //Artifact path was invalid. Couldn't locate the requested path.
                return null;
            }

            try (final CloseableRestResponse closeableRestResponse = client.get(resource.toString()))
            {
                final Response response = closeableRestResponse.getResponse();

                if (response.getStatus() != 200 || response.getEntity() == null)
                {
                    return null;
                }

                InputStream is = response.readEntity(InputStream.class);
                if (is == null)
                {
                    return null;
                }

                is = onSuccessfulProxyRepositoryResponse(is, storageId, repositoryId, path);

                RepositoryPath artifactPath = layoutProvider.resolve(repository).resolve(path);
                RepositoryFileAttributes artifactFileAttributes = Files.readAttributes(artifactPath,
                                                                                       RepositoryFileAttributes.class);
                if (!artifactFileAttributes.isChecksum() && !artifactFileAttributes.isMetadata())
                {
                    artifactEventListenerRegistry.dispatchArtifactFetchedFromRemoteEvent(storageId, repositoryId, path);
                }

                return is;
            }
        }
    }

    protected InputStream onSuccessfulProxyRepositoryResponse(final InputStream is,
                                                              final String storageId,
                                                              final String repositoryId,
                                                              final String path)
            throws IOException, NoSuchAlgorithmException, ProviderImplementationException
    {
        return is;
    }

    protected InputStream preProxyRepositoryAccessAttempt(Repository repository,
                                                          String path)
            throws IOException
    {
        return null;
    }

    protected abstract Logger getLogger();

    protected Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

}
