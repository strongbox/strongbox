package org.carlspring.strongbox.providers.repository.proxied;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Qualifier;

import org.carlspring.commons.io.MultipleDigestInputStream;
import org.carlspring.strongbox.providers.io.RepositoryFiles;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.repository.HostedRepositoryProvider;
import org.carlspring.strongbox.services.ArtifactByteStreamsCopyStrategy;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.services.support.ArtifactByteStreamsCopyStrategyDeterminator;
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
    private HostedRepositoryProvider hostedRepositoryProvider;

    @Inject
    private ArtifactManagementService artifactManagementService;
    
    @Inject
    private ArtifactByteStreamsCopyStrategy proxyRepositoryArtifactByteStreamsCopy;
    
    @Override
    protected InputStream preProxyRepositoryAccessAttempt(RepositoryPath repositoryPath)
            throws IOException
    {
        return hostedRepositoryProvider.getInputStream(repositoryPath);
    }

    @Override
    protected InputStream onSuccessfulProxyRepositoryResponse(InputStream is,
                                                              RepositoryPath repositoryPath)
            throws IOException
    {
        final RepositoryPath tempArtifact = RepositoryFiles.temporary(repositoryPath);
        
        try (// Wrap the InputStream, so we could have checksums to compare
                final InputStream remoteIs = new MultipleDigestInputStream(is))
        {
            artifactManagementService.store(tempArtifact, remoteIs);
            RepositoryFiles.permanent(repositoryPath);
        }
        catch (Exception e)
        {
            Files.delete(tempArtifact);
            throw (IOException) Optional.of(e).filter(t -> t instanceof IOException).orElse(new IOException(e));
        }
        
        // TODO: Add a policy for validating the checksums of downloaded artifacts
        // TODO: Validate the local checksum against the remote's checksums
        // sbespalov: we have checksum validation within ArtifactManagementService.store() method, but it's not strict for now (see SB-949)
        
        // Serve the downloaded artifact
        return Files.newInputStream(repositoryPath);
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
