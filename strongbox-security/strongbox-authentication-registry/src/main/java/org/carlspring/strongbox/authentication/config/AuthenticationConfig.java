package org.carlspring.strongbox.authentication.config;

import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.authentication.registry.support.AuthenticatorsScanner;
import org.carlspring.strongbox.authentication.external.ExternalUserProvidersFileManager;

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
        return new AuthenticatorsRegistry();
    }

    @Bean
    ExternalUserProvidersFileManager authenticationProvidersFileManager()
    {
        return new ExternalUserProvidersFileManager();
    }
    
}
