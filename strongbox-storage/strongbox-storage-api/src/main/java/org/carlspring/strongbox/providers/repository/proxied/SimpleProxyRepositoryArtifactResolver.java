package org.carlspring.strongbox.providers.repository.proxied;

import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.services.support.ArtifactByteStreamsCopyStrategyDeterminator;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This implementation of {@link ProxyRepositoryArtifactResolver} does not try to resolve the artifact
 * from the local cache storage. It always fetches the artifact from the proxy/remote
 * repository and it never stores the artifact in the local cache storage.
 * 
 * @author Przemyslaw Fusik
 */
@Component
@SimpleProxyRepositoryArtifactResolver.SimpleProxyRepositoryArtifactResolverQualifier
public class SimpleProxyRepositoryArtifactResolver
        extends ProxyRepositoryArtifactResolver
{

    private static final Logger logger = LoggerFactory.getLogger(SimpleProxyRepositoryArtifactResolver.class);

    @Inject
    private ArtifactByteStreamsCopyStrategyDeterminator artifactByteStreamsCopyStrategyDeterminator;

    @Override
    protected InputStream onSuccessfulProxyRepositoryResponse(InputStream is,
                                                              RepositoryPath repositoryPath)
            throws IOException
    {
        final Path tempDir = Paths.get(ConfigurationResourceResolver.getTempDirectory());
        final Path tempPath = Files.createTempFile(tempDir, "strongbox", ".tmp");

        Repository repository = repositoryPath.getFileSystem().getRepository();
        // buffered in ArtifactByteStreamsCopyStrategy.copy()
        try (final OutputStream tempPathOs = Files.newOutputStream(tempPath))
        {
            artifactByteStreamsCopyStrategyDeterminator.determine(repository).copy(is, tempPathOs, repositoryPath);
        }

        ResourceCloser.close(is, logger);
        return new BufferedInputStream(Files.newInputStream(tempPath));
    }

    @Override
    protected Logger getLogger()
    {
        return logger;
    }

    @Documented
    @Retention(RUNTIME)
    @Qualifier
    public @interface SimpleProxyRepositoryArtifactResolverQualifier
    {

    }
}
