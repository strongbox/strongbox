package org.carlspring.strongbox.providers.repository.proxied;

import org.carlspring.strongbox.client.RemoteRepositoryRetryArtifactDownloadConfiguration;
import org.carlspring.strongbox.client.RestArtifactResolver;
import org.carlspring.strongbox.client.config.ProxyRepositoryConnectionConfigurationService;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.storage.repository.remote.heartbeat.RemoteRepositoryAlivenessService;

import javax.inject.Inject;

import java.net.MalformedURLException;
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
    private ProxyRepositoryConnectionConfigurationService proxyRepositoryConnectionConfigurationService;

    @Inject
    private ConfigurationManager configurationManager;

    @Inject
    private RemoteRepositoryAlivenessService remoteRepositoryAlivenessCacheManager;

    public RestArtifactResolver newInstance(RemoteRepository repository)
        throws MalformedURLException
    {
        Objects.requireNonNull(repository);

        RemoteRepositoryRetryArtifactDownloadConfiguration configuration = configurationManager.getConfiguration()
                                                                                               .getRemoteRepositoriesConfiguration()
                                                                                               .getRemoteRepositoryRetryArtifactDownloadConfiguration();

        String username = repository.getUsername();
        String password = repository.getPassword();
        String url = repository.getUrl();

        final HttpAuthenticationFeature authenticationFeature = (username != null && password != null)
                ? HttpAuthenticationFeature.basic(username, password) : null;

        return new RestArtifactResolver(
                proxyRepositoryConnectionConfigurationService.getClientForRepository(repository), url,
                configuration,
                authenticationFeature)
        {

            @Override
            public boolean isAlive()
            {
                return remoteRepositoryAlivenessCacheManager.isAlive(repository);
            }

        };
    }

}
