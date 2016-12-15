package org.carlspring.strongbox.providers.repository;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;

import org.apache.maven.artifact.Artifact;
import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.commons.io.resource.ResourceCloser;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.artifact.coordinates.MavenArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactResolver;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.io.ArtifactFile;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.io.ArtifactPath;
import org.carlspring.strongbox.providers.layout.LayoutProviderRegistry;
import org.carlspring.strongbox.providers.storage.StorageProvider;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.RemoteRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class ProxyRepositoryProvider extends AbstractRepositoryProvider
{

    private static final Logger logger = LoggerFactory.getLogger(ProxyRepositoryProvider.class);

    private static final String ALIAS = "proxy";

    @Autowired
    private RepositoryProviderRegistry repositoryProviderRegistry;

    @Autowired
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;


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
        StorageProvider storageProvider = getStorageProviderRegistry().getProvider(repository.getImplementation());
        Artifact artifact = ArtifactUtils.convertPathToArtifact(path);
        ArtifactCoordinates coordinates = new MavenArtifactCoordinates(artifact);
        ArtifactPath artifactPath = storageProvider.resolve(repository, coordinates);
        logger.debug(" -> Checking for " + artifactPath + "...");

        if (Files.exists(artifactPath))
        {
            ArtifactFile artifactFile = artifactPath.toFile();

            logger.debug("The artifact was found in the local cache.");
            logger.debug("Resolved " + artifactFile.getCanonicalPath() + "!");

            ArtifactInputStream ais = new ArtifactInputStream(coordinates, new FileInputStream(artifactFile));
            ais.setLength(artifactFile.length());

            return ais;
        }
        else
        {
            logger.debug("The artifact was not found in the local cache.");

            ArtifactFile artifactFile = new ArtifactFile(repository.getBasedir(), coordinates, true);

            RemoteRepository remoteRepository = repository.getRemoteRepository();

            ArtifactResolver client = new ArtifactResolver(proxyRepositoryConnectionPoolConfigurationService.getClient());
            client.setRepositoryBaseUrl(remoteRepository.getUrl());
            client.setUsername(remoteRepository.getUsername());
            client.setPassword(remoteRepository.getPassword());

            Response response = client.getResourceWithResponse(coordinates.toPath());
            if (response.getStatus() != 200 || response.getEntity() == null)
            {
                return null;
            }

            logger.debug("Creating " + artifactFile.getTemporaryFile().getParentFile().getAbsolutePath() + "...");

            artifactFile.createParents();

            InputStream remoteIs = response.readEntity(InputStream.class);
            FileOutputStream fos = new FileOutputStream(artifactFile.getTemporaryFile());
            MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(fos);

            try
            {
                // 1) Attempt to resolve it from the remote host
                if (remoteIs == null)
                {
                    // 1 a) If the artifact does not exist, return null
                    // The remote failed to resolve the artifact as well.
                    return null;
                }

                int len;
                final int size = 1024;
                byte[] bytes = new byte[size];

                while ((len = remoteIs.read(bytes, 0, size)) != -1)
                {
                    mdos.write(bytes, 0, len);
                }

                mdos.flush();
            }
            finally
            {
                ResourceCloser.close(mdos, logger);
                ResourceCloser.close(fos, logger);
                ResourceCloser.close(remoteIs, logger);
                ResourceCloser.close(client, logger);
            }

            // TODO: Add a policy for validating the checksums of downloaded artifacts
            // TODO: Validate the local checksum against the remote's checksums

            artifactFile.moveTempFileToOriginalDestination();

            // 1 b) If it exists on the remote, serve the downloaded artifact

            ArtifactInputStream ais = new ArtifactInputStream(coordinates, new FileInputStream(artifactFile));
            ais.setLength(artifactFile.length());

            return ais;
        }
    }

    @Override
    public OutputStream getOutputStream(String storageId,
                                        String repositoryId,
                                        String artifactPath)
            throws IOException
    {
        // It should not be possible to write artifacts to a proxy repository.
        // A proxy repository should only serve artifacts that already exist
        // in the cache, or the remote host.

        return null;
    }

}
