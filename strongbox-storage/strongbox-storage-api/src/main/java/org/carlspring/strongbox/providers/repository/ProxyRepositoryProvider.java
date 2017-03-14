package org.carlspring.strongbox.providers.repository;

import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.client.ArtifactResolver;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactOutputStream;
import org.carlspring.strongbox.io.RepositoryFileSystemProvider;
import org.carlspring.strongbox.io.RepositoryPath;
import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.RemoteRepository;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class ProxyRepositoryProvider extends AbstractRepositoryProvider
{

    private static final Logger logger = LoggerFactory.getLogger(ProxyRepositoryProvider.class);

    private static final String ALIAS = "proxy";

    @Inject
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @Inject
    @Qualifier("filesystemStorageProvider")
    private StorageProvider filesystemStorageProvider;

    @PostConstruct
    @Override
    public void register()
    {
        repositoryProviderRegistry.addProvider(ALIAS, this);

        logger.info("Registered repository provider '" + getClass().getCanonicalName() + "' with alias '" + ALIAS + "'.");
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
                   ArtifactTransportException
    {
        Storage storage = getConfiguration().getStorage(storageId);

        logger.debug("Checking in " + storage.getId() + ":" + repositoryId + "...");

        Repository repository = getConfiguration().getStorage(storageId).getRepository(repositoryId);

        RepositoryPath reposytoryPath = filesystemStorageProvider.resolve(repository);
        RepositoryPath artifactPath = reposytoryPath.resolve(path);
        RepositoryFileSystemProvider fileSystemProvider = (RepositoryFileSystemProvider) artifactPath.getFileSystem()
                                                                                                     .provider();

        logger.debug(" -> Checking for " + artifactPath + "...");

        if (Files.exists(artifactPath))
        {
            logger.debug("The artifact was found in the local cache.");
            logger.debug("Resolved " + artifactPath + "!");

            return new ArtifactInputStream(null, Files.newInputStream(artifactPath));
        }
        else
        {
            logger.debug("The artifact was not found in the local cache.");

            RepositoryPath tempArtifact = fileSystemProvider.getTempPath(artifactPath);
            
            RemoteRepository remoteRepository = repository.getRemoteRepository();

            ArtifactResolver client = new ArtifactResolver(proxyRepositoryConnectionPoolConfigurationService.getClient());
            client.setRepositoryBaseUrl(remoteRepository.getUrl());
            client.setUsername(remoteRepository.getUsername());
            client.setPassword(remoteRepository.getPassword());

            Response response = client.getResourceWithResponse(path);
            if (response.getStatus() != 200 || response.getEntity() == null)
            {
                return null;
            }

            InputStream remoteIs = response.readEntity(InputStream.class);
            if (remoteIs == null)
            {
                return null;
            }

            int total = 0;
            try (MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(fileSystemProvider.newOutputStream(
                    tempArtifact)))
            {
                int len;
                final int size = 1024;
                byte[] bytes = new byte[size];

                while ((len = remoteIs.read(bytes, 0, size)) != -1)
                {
                    mdos.write(bytes, 0, len);
                    total += len;
                }

                mdos.flush();
            }


            // TODO: Add a policy for validating the checksums of downloaded artifacts
            // TODO: Validate the local checksum against the remote's checksums
            fileSystemProvider.restoreFromTemp(artifactPath);

            // 1 b) If it exists on the remote, serve the downloaded artifact
            return new ArtifactInputStream(null, Files.newInputStream(artifactPath));
        }
    }

    @Override
    public ArtifactOutputStream getOutputStream(String storageId,
                                                String repositoryId,
                                                String artifactPath)
            throws IOException
    {
        // It should not be possible to write artifacts to a proxy repository.
        // A proxy repository should only serve artifacts that already exist
        // in the cache, or the remote host.
        throw new UnsupportedOperationException();
    }

}
