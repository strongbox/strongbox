package org.carlspring.strongbox.authentication.registry.support;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.carlspring.strongbox.authentication.registry.AuthenticationProvidersRegistry;
import org.carlspring.strongbox.authentication.registry.AuthenticationProvidersRegistry.AuthenticationConfigurationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * @author Przemyslaw Fusik
 */
public class ConfigurableProviderManager extends ProviderManager
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurableProviderManager.class);

    @Inject
    private AuthenticationProvidersRegistry authenticationProvidersRegistry;

    public ConfigurableProviderManager()
    {
        super(new ArrayList<>(), new EmptyAuthenticationManager());
    }

    public void reloadRegistry()
    {
        authenticationProvidersRegistry.scanAndReloadRegistry();
        
        List<AuthenticationProvider> providerList = this.getProviders();
        providerList.clear();
        
        authenticationProvidersRegistry.forEach(p -> providerList.add(p));
    }

    @EventListener({ ContextRefreshedEvent.class })
    void contextRefreshedEvent(ContextRefreshedEvent e)
    {
        ApplicationContext applicationContext = e.getApplicationContext();
        if (applicationContext == null || applicationContext instanceof AuthenticationConfigurationContext)
        {
            return;
        }
        
        reloadRegistry();
    }
    
    public static final class EmptyAuthenticationManager implements AuthenticationManager {

        @Override
        public Authentication authenticate(Authentication authentication)
            throws AuthenticationException
        {
            throw new BadCredentialsException("invalid.credentials");
        }
        
    }

    public static final class AuthenticationNotConfiguredException extends AuthenticationException {

        public AuthenticationNotConfiguredException()
        {
            super("Authentication should be configured with `strongbox-authentication-providers.xml`");
        }
        
    }
}
