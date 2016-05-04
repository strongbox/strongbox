package org.carlspring.strongbox.config;

import org.carlspring.strongbox.security.jaas.caching.CachedUserManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ComponentScan({
        "org.carlspring.strongbox.configuration",
        "org.carlspring.strongbox.security",
        "org.carlspring.strongbox.visitors",
})
@Import({
        CommonConfig.class
})
public class StrongboxSecurityConfig
{

    @Bean(name = "cachedUserManager")
    CachedUserManager cachedUserManager()
    {
        CachedUserManager cachedUserManager = new CachedUserManager();
        cachedUserManager.setCredentialExpiredCheckInterval(60000);
        cachedUserManager.setCredentialsLifetime(300000);

        return cachedUserManager;
    }
}
