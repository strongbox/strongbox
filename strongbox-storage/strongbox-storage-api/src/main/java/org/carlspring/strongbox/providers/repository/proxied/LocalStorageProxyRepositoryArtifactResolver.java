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
import org.carlspring.strongbox.artifact.coordinates.ArtifactCoordinates;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.domain.ArtifactEntry;
import org.carlspring.strongbox.domain.RemoteArtifactEntry;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemProvider;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
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
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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
            throws IOException,
                   NoSuchAlgorithmException,
                   ArtifactTransportException,
                   ProviderImplementationException
    {
        final Storage storage = repository.getStorage();

        final Optional<ArtifactEntry> artifactEntry = artifactEntryService.findOneArtifact(storage.getId(),
                                                                                            repository.getId(),
                                                                                            path);
        final InputStream result = hostedRepositoryProvider.getInputStream(storage.getId(), repository.getId(), path);

        if (artifactEntry.isPresent())
        {
            final ArtifactEntry artifactEntryItself = artifactEntry.get();
            artifactEntryItself.setLastUsed(new Date());
            artifactEntryService.save(artifactEntryItself);

            if (result == null)
            {
                logger.error(
                        "ArtifactEntry {} was found in the database but not found in the file system. Possible synchronization issue.",
                        artifactEntryItself);
            }
        }
        else if (result != null)
        {
            logger.error(
                    "ArtifactEntry was not found in the database for artifact [{} {} {}] but found in the file system. Possible synchronization issue.",
                    storage.getId(), repository.getId(), path);
        }

        return result;
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
            long totalNumberOfArtifactBytes = layoutProvider.getArtifactManagementService().store(tempArtifact,
                                                                                                  remoteIs);

            // TODO: Add a policy for validating the checksums of downloaded artifacts
            // TODO: Validate the local checksum against the remote's checksums
            fileSystemProvider.moveFromTemporaryDirectory(artifactPath);

            RemoteArtifactEntry artifactEntry = (RemoteArtifactEntry) artifactEntryService.findOneArtifact(storageId,
                                                                                                           repositoryId,
                                                                                                           path)
                                                                                          .orElse(new RemoteArtifactEntry());

            ArtifactCoordinates c = RepositoryFiles.readCoordinates(artifactPath);
            artifactEntry.setArtifactCoordinates(c);
            artifactEntry.setStorageId(storageId);
            artifactEntry.setRepositoryId(repositoryId);
            artifactEntry.setIsCached(Boolean.TRUE);
            artifactEntry.setArtifactPath(path);
            Date now = new Date();
            artifactEntry.setLastUpdated(now);
            artifactEntry.setLastUsed(now);
            artifactEntry.setSizeInBytes(totalNumberOfArtifactBytes);
            artifactEntryService.save(artifactEntry);

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
