package org.carlspring.strongbox.authentication.config;

import javax.inject.Inject;

import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.authentication.registry.support.AuthenticatorsScanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;

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
    
}
