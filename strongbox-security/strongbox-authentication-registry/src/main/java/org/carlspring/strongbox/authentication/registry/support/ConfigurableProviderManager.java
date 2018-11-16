package org.carlspring.strongbox.authentication.registry.support;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.carlspring.strongbox.authentication.registry.AuthenticationProvidersRegistry;
import org.carlspring.strongbox.authentication.registry.AuthenticationProvidersRegistry.AuthenticationConfigurationContext;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.InMemoryUserService;
import org.carlspring.strongbox.users.userdetails.StrongboxUserActualizer;
import org.carlspring.strongbox.users.userdetails.UserDetailsMapper;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @author Przemyslaw Fusik
 */
public class ConfigurableProviderManager extends ProviderManager implements UserDetailsService
{

    public static final int USER_INVALIDATE_SECONDS = 300;

    private static final Logger logger = LoggerFactory.getLogger(ConfigurableProviderManager.class);

    @Inject
    private AuthenticationProvidersRegistry authenticationProvidersRegistry;

    @Inject
    @InMemoryUserService.InMemoryUserServiceQualifier
    private UserService userService;

    @Inject
    private UserDetailsMapper springSecurityUserMapper;

    @Inject
    private StrongboxUserActualizer strongboxUserActualizer;

    private final List<AuthenticationProvider> providerList;

    private final List<UserDetailsService> userDetailServiceList;

    public ConfigurableProviderManager()
    {
        super(new ArrayList<>(), new EmptyAuthenticationManager());

        providerList = getProviders();
        userDetailServiceList = new ArrayList<>();
    }

    public void reloadRegistry()
    {
        authenticationProvidersRegistry.scanAndReloadRegistry();

        providerList.clear();
        authenticationProvidersRegistry.forEach(p -> providerList.add(p));

        userDetailServiceList.clear();
        userDetailServiceList.addAll(authenticationProvidersRegistry.getUserDetailsServices());
    }

    @Override
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException
    {
        User user = loadUserDetails(username);

        if (user == null)
        {
            throw new UsernameNotFoundException(username);
        }

        return springSecurityUserMapper.apply(user);
    }

    private User loadUserDetails(String username)
    {
        User user = userService.findByUserName(username);

        Date userLastUpdate = Optional.ofNullable(user)
                                      .flatMap(u -> Optional.ofNullable(u.getLastUpdate()))
                                      .orElse(Date.from(Instant.EPOCH));
        Date userExpireDate = Date.from(Instant.ofEpochMilli(userLastUpdate.getTime())
                                               .plusSeconds(USER_INVALIDATE_SECONDS));
        Date nowDate = new Date();
        if (user != null && nowDate.before(userExpireDate))
        {
            return user;
        }

        UserDetails externalUserDetails = null;
        for (UserDetailsService userDetailsService : userDetailServiceList)
        {
            try
            {
                externalUserDetails = userDetailsService.loadUserByUsername(username);
            }
            catch (UsernameNotFoundException e)
            {
                continue;
            }

            break;
        }

        if (externalUserDetails == null)
        {
            userService.delete(username);

            return null;
        }

        return strongboxUserActualizer.apply(externalUserDetails);
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

    public static final class EmptyAuthenticationManager implements AuthenticationManager
    {

        @Override
        public Authentication authenticate(Authentication authentication)
            throws AuthenticationException
        {
            throw new BadCredentialsException("invalid.credentials");
        }

    }

    public static final class AuthenticationNotConfiguredException extends AuthenticationException
    {

        public AuthenticationNotConfiguredException()
        {
            super("Authentication should be configured with `strongbox-authentication-providers.xml`");
        }

    }

}
