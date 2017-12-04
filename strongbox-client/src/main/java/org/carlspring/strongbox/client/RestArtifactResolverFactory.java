package org.carlspring.strongbox.client;

import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;

import javax.inject.Inject;
import javax.ws.rs.core.Feature;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class RestArtifactResolverFactory
{

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    public RestArtifactResolver newInstance(final String url,
                                            final Feature authentication)
    {
        return new RestArtifactResolver(proxyRepositoryConnectionPoolConfigurationService.getRestClient(), url,
                                        authentication);
    }

}
