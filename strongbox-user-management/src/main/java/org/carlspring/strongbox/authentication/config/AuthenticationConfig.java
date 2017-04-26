package org.carlspring.strongbox.authentication.config;

import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.authentication.registry.support.AuthenticatorsClassLoader;
import org.carlspring.strongbox.authentication.registry.support.AuthenticatorsScanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
public class AuthenticationConfig
{

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationConfig.class);

    @Bean
    AuthenticatorsScanner authenticatorsScanner(AuthenticatorsRegistry authenticatorsRegistry)
    {
        return new AuthenticatorsScanner(authenticatorsRegistry);
    }

    @Bean
    AuthenticatorsRegistry authenticatorsRegistry()
    {
        AuthenticatorsClassLoader.loadAuthenticatorsClasses();
        AuthenticatorsRegistry authenticatorsRegistry = new AuthenticatorsRegistry();
        AuthenticatorsScanner authenticatorsScanner = authenticatorsScanner(authenticatorsRegistry);
        authenticatorsScanner.scanAndReloadRegistry();

        return authenticatorsRegistry;
    }
}
