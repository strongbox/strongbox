package org.carlspring.strongbox.providers.repository.proxied;

import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.io.ArtifactInputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileAttributes;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemProvider;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.providers.repository.HostedRepositoryProvider;
import org.carlspring.strongbox.services.ArtifactEntryService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
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

        final RepositoryFileSystemProvider fileSystemProvider = artifactPath.getFileSystem().provider();
        final RepositoryPath tempArtifact = fileSystemProvider.getTempPath(artifactPath);

        try (// Wrap the InputStream, so we could have checksums to compare
             final InputStream remoteIs = new MultipleDigestInputStream(is))
        {
            layoutProvider.getArtifactManagementService().store(tempArtifact, remoteIs);

            // TODO: Add a policy for validating the checksums of downloaded artifacts
            // TODO: Validate the local checksum against the remote's checksums
            fileSystemProvider.moveFromTemporaryDirectory(artifactPath);

            // Serve the downloaded artifact
            ArtifactInputStream result = (ArtifactInputStream) Files.newInputStream(artifactPath);

            ArtifactCoordinates c = (ArtifactCoordinates) Files.getAttribute(artifactPath, RepositoryFileAttributes.COORDINATES);
            String p = artifactPath.getResourceLocation();

            RemoteArtifactEntry artifactEntry = (RemoteArtifactEntry) artifactEntryService.findOneAritifact(storageId,
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
