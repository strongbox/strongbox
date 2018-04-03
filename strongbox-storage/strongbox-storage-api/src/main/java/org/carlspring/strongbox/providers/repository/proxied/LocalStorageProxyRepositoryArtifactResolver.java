package org.carlspring.strongbox.providers.repository.proxied;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import javax.inject.Inject;
import javax.inject.Qualifier;

import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.repository.HostedRepositoryProvider;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This implementation of {@link ProxyRepositoryArtifactResolver} first tries to resolve the artifact
 * from the local cache storage. If there is not artifact found, it will be fetched from proxy/remote
 * repository and then stored in the local cache storage for subsequent requests.
 *
 * @author Przemyslaw Fusik
 */
@Component
@LocalStorageProxyRepositoryArtifactResolver.LocalStorageProxyRepositoryArtifactResolverQualifier
public class LocalStorageProxyRepositoryArtifactResolver
        extends ProxyRepositoryArtifactResolver
{

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageProxyRepositoryArtifactResolver.class);

    @Inject
    private ArtifactEntryService artifactEntryService;

    @Inject
    private HostedRepositoryProvider hostedRepositoryProvider;

    @Override
    protected InputStream preProxyRepositoryAccessAttempt(final Repository repository,
                                                          final String path)
            throws IOException
    {
        Storage storage = repository.getStorage();

        return hostedRepositoryProvider.getInputStream(storage.getId(), repository.getId(), path);
    }

    @Override
    protected InputStream onSuccessfulProxyRepositoryResponse(InputStream is,
                                                              String storageId,
                                                              String repositoryId,
                                                              String path)
            throws IOException, NoSuchAlgorithmException, ProviderImplementationException
    {
        final Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        final Repository repository = storage.getRepository(repositoryId);

        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final RepositoryPath artifactPath = layoutProvider.resolve(repository).resolve(path);
        final RepositoryPath tempArtifact = RepositoryFiles.temporary(artifactPath);

        try (// Wrap the InputStream, so we could have checksums to compare
             final InputStream remoteIs = new MultipleDigestInputStream(is))
        {
            layoutProvider.getArtifactManagementService().store(tempArtifact, remoteIs);

            // TODO: Add a policy for validating the checksums of downloaded artifacts
            // TODO: Validate the local checksum against the remote's checksums
            // sbespalov: we have checksum validation within ArtifactManagementService.store() method, but it's not strict for now (see SB-949)
            RepositoryFiles.permanent(artifactPath);

            // Serve the downloaded artifact
            return Files.newInputStream(artifactPath);
        }
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    @Documented
    @Retention(RUNTIME)
    @Qualifier
    public @interface LocalStorageProxyRepositoryArtifactResolverQualifier
    {

    }
}
