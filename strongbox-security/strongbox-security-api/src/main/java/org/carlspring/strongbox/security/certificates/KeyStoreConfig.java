package org.carlspring.strongbox.security.certificates;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Przemyslaw Fusik
 */
@Configuration
public class KeyStoreConfig
{

    @Bean
    @ConditionalOnProperty(prefix = "server.ssl", name = "key-store")
    KeyStoreManager keyStoreManager()
    {
        return new KeyStoreManager();
    }

    @Bean
    @ConditionalOnProperty(prefix = "server.ssl", name = "trust-store")
    TrustStoreManager trustStoreManager()
    {
        return new TrustStoreManager();
    }
}
