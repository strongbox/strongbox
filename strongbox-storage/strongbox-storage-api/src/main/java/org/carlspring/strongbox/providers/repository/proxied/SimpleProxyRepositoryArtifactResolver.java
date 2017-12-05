package org.carlspring.strongbox.providers.repository.proxied;

import org.carlspring.strongbox.providers.ProviderImplementationException;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.layout.LayoutProvider;
import org.carlspring.strongbox.services.support.ArtifactByteStreamsCopyStrategyDeterminator;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import javax.inject.Qualifier;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
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
                                                              String storageId,
                                                              String repositoryId,
                                                              String path)
            throws IOException, NoSuchAlgorithmException, ProviderImplementationException
    {
        final Path tempPath = Files.createTempFile("strongbox", ".tmp");
        try (final OutputStream tempPathOs = Files.newOutputStream(tempPath))
        {
            final Storage storage = getConfiguration().getStorage(storageId);
            final Repository repository = storage.getRepository(repositoryId);
            final LayoutProvider layoutProvider = layoutProviderRegistry.getProvider(repository.getLayout());
            final RepositoryPath artifactPath = layoutProvider.resolve(repository).resolve(path);
            artifactByteStreamsCopyStrategyDeterminator.determine(repository).copy(is, tempPathOs, artifactPath);
        }
        IOUtils.closeQuietly(is);
        return Files.newInputStream(tempPath);
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
