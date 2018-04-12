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
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessCacheManager;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
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

    public InputStream getInputStream(RepositoryPath repositoryPath)
        throws IOException
    {
        Repository repository = repositoryPath.getFileSystem().getRepository();
        getLogger().debug(String.format("Checking in [%s]...", repositoryPath));

        final InputStream candidate = preProxyRepositoryAccessAttempt(repositoryPath);
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
            URI resource = RepositoryFiles.resolveResource(repositoryPath);
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
                is = new BufferedInputStream(is);

                is = onSuccessfulProxyRepositoryResponse(is, repositoryPath);

                RepositoryFileAttributes artifactFileAttributes = Files.readAttributes(repositoryPath,
                                                                                       RepositoryFileAttributes.class);
                if (!artifactFileAttributes.isChecksum() && !artifactFileAttributes.isMetadata())
                {
                    artifactEventListenerRegistry.dispatchArtifactFetchedFromRemoteEvent(repositoryPath);
                }

                return is;
            }
        }
    }

    protected InputStream onSuccessfulProxyRepositoryResponse(final InputStream is,
                                                              final RepositoryPath repositoryPath)
            throws IOException
    {
        return is;
    }

    protected InputStream preProxyRepositoryAccessAttempt(RepositoryPath p)
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
