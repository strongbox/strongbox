package org.carlspring.strongbox.authentication.registry;

import org.carlspring.strongbox.authentication.external.ExternalUserProvidersFileManager;
import org.carlspring.strongbox.authentication.registry.support.ConfigurableProviderManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
@ComponentScan
public class AuthenticationConfig
{

    @Bean
    @Primary
    ConfigurableProviderManager authenticationManager()
    {
        return new ConfigurableProviderManager();
    }

    @Bean
    ExternalUserProvidersFileManager externalUserProvidersFileManager()
    {
        return new ExternalUserProvidersFileManager();
    }

}
