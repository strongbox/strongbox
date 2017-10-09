package org.carlspring.strongbox.providers.repository.proxied;

import javax.inject.Qualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

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
