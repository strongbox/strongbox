package org.carlspring.strongbox.client;

import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Objects;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RestArtifactResolverFactory
{

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    public RestArtifactResolver newInstance(@Nonnull  final String url,
                                            @Nullable final String username,
                                            @Nullable final String password)
    {
        Objects.requireNonNull(url);
        final HttpAuthenticationFeature authenticationFeature =
                (username != null && password != null) ? HttpAuthenticationFeature.basic(username, password) : null;
        return new RestArtifactResolver(proxyRepositoryConnectionPoolConfigurationService.getRestClient(), url,
                                        authenticationFeature);
    }

}
