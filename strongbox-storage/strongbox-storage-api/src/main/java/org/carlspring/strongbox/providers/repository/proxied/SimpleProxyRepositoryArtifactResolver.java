package org.carlspring.strongbox.providers.repository.proxied;

import org.carlspring.strongbox.providers.ProviderImplementationException;

import javax.inject.Qualifier;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.FileUtils;
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

    @Override
    protected InputStream post(InputStream is,
                               String storageId,
                               String repositoryId,
                               String path)
            throws IOException, NoSuchAlgorithmException, ProviderImplementationException
    {
        final java.io.File file = java.io.File.createTempFile("strongbox", ".tmp");
        FileUtils.copyInputStreamToFile(is, file);
        return Files.newInputStream(file.toPath());
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
