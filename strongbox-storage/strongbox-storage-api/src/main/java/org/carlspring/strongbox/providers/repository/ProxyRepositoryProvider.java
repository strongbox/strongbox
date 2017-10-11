package org.carlspring.strongbox.providers.repository;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactResolver;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemProvider;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @Inject
    private LayoutProviderRegistry layoutProviderRegistry;

    @Inject
    private RemoteRepositoryAlivenessCacheManager remoteRepositoryAlivenessCacheManager;

    @Inject
    private ArtifactEntryService artifactEntryService;
    
    @PostConstruct
    @Override
    public void register()
    {
        repositoryProviderRegistry.addProvider(ALIAS, this);

        logger.info("Registered repository provider '" + getClass().getCanonicalName() +
                    "' with alias '" + ALIAS + "'.");
    }

    @Override
    public String getAlias()
    {
        return ALIAS;
    }

    @Override
    public ArtifactInputStream getInputStream(String storageId,
                                              String repositoryId,
                                              String path)
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        logger.debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        RepositoryPath reposytoryPath = layoutProvider.resolve(repository);
        RepositoryPath artifactPath = reposytoryPath.resolve(path);

        logger.debug(" -> Checking for " + artifactPath + "...");
        if (layoutProvider.containsPath(repository, path))
        {
            logger.debug("The artifact was found in the local cache.");
            logger.debug("Resolved " + artifactPath + "!");

            RepositoryPath repositoryPath = layoutProvider.resolve(repository).resolve(path);
            return (ArtifactInputStream) Files.newInputStream(repositoryPath);
        }
        
        logger.debug("The artifact was not found in the local cache.");
        RemoteRepository remoteRepository = repository.getRemoteRepository();
        if (!remoteRepositoryAlivenessCacheManager.isAlive(remoteRepository))
        {
            logger.debug("Remote repository '" + remoteRepository.getUrl() + "' is down.");

            return null;
        }

        ArtifactResolver client = new ArtifactResolver(proxyRepositoryConnectionPoolConfigurationService.getRestClient());
        client.setRepositoryBaseUrl(remoteRepository.getUrl());
        client.setUsername(remoteRepository.getUsername());
        client.setPassword(remoteRepository.getPassword());

        String resourcePath = layoutProvider.resolveResourcePath(repository, path);
        
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

            RepositoryFileSystemProvider fileSystemProvider = artifactPath.getFileSystem().provider();
            RepositoryPath tempArtifact = fileSystemProvider.getTempPath(artifactPath);
            try (// Wrap the InputStream, so we could have checksums to compare
                 InputStream remoteIs = new MultipleDigestInputStream(is))
            {
                layoutProvider.getArtifactManagementService().store(tempArtifact, remoteIs);

                // TODO: Add a policy for validating the checksums of downloaded artifacts
                // TODO: Validate the local checksum against the remote's checksums
                fileSystemProvider.moveFromTemporaryDirectory(artifactPath);

                // Serve the downloaded artifact
                ArtifactInputStream result = (ArtifactInputStream) Files.newInputStream(artifactPath);
                
                ArtifactCoordinates c = (ArtifactCoordinates) Files.getAttribute(artifactPath,RepositoryFileAttributes.COORDINATES);
                String p = artifactPath.getRepositoryRelative().toString();
                
                RemoteArtifactEntry artifactEntry = (RemoteArtifactEntry) artifactEntryService.findOne(storageId,
                                                                                                       repositoryId,
                                                                                                       p)
                                                                                              .orElse(new RemoteArtifactEntry());
                artifactEntry.setArtifactCoordinates(c);
                artifactEntry.setRepositoryId(storageId);
                artifactEntry.setRepositoryId(repositoryId);
                artifactEntry.setIsCached(Boolean.TRUE);
                artifactEntry.setArtifactPath(p);
                artifactEntryService.save(artifactEntry);
                
                return result;
            }
        }
    }

    @Override
    public ArtifactOutputStream getOutputStream(String storageId,
                                                String repositoryId,
                                                String artifactPath)
            throws IOException,
                   NoSuchAlgorithmException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        Repository repository = storage.getRepository(repositoryId);

        LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        RepositoryPath repositoryPath = layoutProvider.resolve(repository).resolve(artifactPath);

        return (ArtifactOutputStream) Files.newOutputStream(repositoryPath);
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
