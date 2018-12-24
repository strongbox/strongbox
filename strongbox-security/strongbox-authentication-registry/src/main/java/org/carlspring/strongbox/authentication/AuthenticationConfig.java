package org.carlspring.strongbox.authentication;

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
    public ConfigurableProviderManager authenticationManager()
    {
        return new ConfigurableProviderManager();
    }

}
