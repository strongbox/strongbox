package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.security.managers.AuthenticationManager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@ComponentScan({
        "org.carlspring.strongbox.configuration",
        "org.carlspring.strongbox.security",
        "org.carlspring.strongbox.visitors",
})
public class StrongboxSecurityConfig
{

    @Bean(name = "authenticationManager", initMethod = "load")
    @Lazy
    AuthenticationManager authenticationManager()
    {
        return new AuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}
