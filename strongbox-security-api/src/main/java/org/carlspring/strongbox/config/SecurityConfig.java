package org.carlspring.strongbox.config;

import org.carlspring.strongbox.security.jaas.caching.CachedUserManager;
import org.carlspring.strongbox.security.jaas.managers.AuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class SecurityConfig {

    @Bean(name = "authenticationManager", initMethod = "load")
    @Lazy
    AuthenticationManager authenticationManager() {
        return new AuthenticationManager();
    }

    @Bean(name = "cachedUserManager")
    CachedUserManager cachedUserManager() {
        CachedUserManager cachedUserManager = new CachedUserManager();
        cachedUserManager.setCredentialExpiredCheckInterval(60000);
        cachedUserManager.setCredentialsLifetime(300000);

        return cachedUserManager;
    }
}
