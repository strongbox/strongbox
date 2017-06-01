package org.carlspring.strongbox.authentication.config;

import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.authentication.registry.support.ExternalAuthenticatorsHelper;
import org.carlspring.strongbox.authentication.registry.support.AuthenticatorsScanner;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
public class AuthenticationConfig
{

    @Bean
    AuthenticatorsScanner authenticatorsScanner(AuthenticatorsRegistry authenticatorsRegistry)
    {
        return new AuthenticatorsScanner(authenticatorsRegistry);
    }

    @Bean
    AuthenticatorsRegistry authenticatorsRegistry()
    {
        ExternalAuthenticatorsHelper.addExternalAuthenticatorsForClassLoading();
        final AuthenticatorsRegistry authenticatorsRegistry = new AuthenticatorsRegistry();
        final AuthenticatorsScanner authenticatorsScanner = authenticatorsScanner(authenticatorsRegistry);
        authenticatorsScanner.scanAndReloadRegistry();

        return authenticatorsRegistry;
    }
}
