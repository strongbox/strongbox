package org.carlspring.strongbox.providers.repository.proxied;

import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryFileSystemProvider;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

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

    @Override
    protected InputStream preRemoteRepositoryAttempt(final Repository repository,
                                                     final String path)
            throws IOException
    {

        final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
        final RepositoryPath artifactPath = layoutProvider.resolve(repository).resolve(path);

        logger.debug(" -> Checking local cache for {} ...", artifactPath);
        if (layoutProvider.containsPath(repository, path))
        {
            logger.debug("The artifact {} was found in the local cache", artifactPath);
            return Files.newInputStream(artifactPath);
        }

        logger.debug("The artifact {} as not found in the local cache", artifactPath);
        return null;
    }

    @Override
    protected InputStream post(InputStream is,
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
