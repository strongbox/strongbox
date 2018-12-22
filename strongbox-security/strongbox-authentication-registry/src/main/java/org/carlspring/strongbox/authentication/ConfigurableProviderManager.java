package org.carlspring.strongbox.authentication;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.carlspring.strongbox.authentication.api.AuthenticationItem;
import org.carlspring.strongbox.authentication.api.AuthenticationItemConfigurationManager;
import org.carlspring.strongbox.authentication.api.AuthenticationItems;
import org.carlspring.strongbox.authentication.api.CustomAuthenticationItemMapper;
import org.carlspring.strongbox.authentication.registry.AuthenticationProvidersRegistry;
import org.carlspring.strongbox.authentication.registry.AuthenticationProvidersRegistry.MergePropertiesContext;
import org.carlspring.strongbox.authentication.support.AuthenticationConfigurationContext;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.InMemoryUserService;
import org.carlspring.strongbox.users.userdetails.StrongboxUserActualizer;
import org.carlspring.strongbox.users.userdetails.UserDetailsMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;
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
 * @author sbespalov
 *
 */
@Primary
public class ConfigurableProviderManager extends ProviderManager implements UserDetailsService, AuthenticationItemConfigurationManager
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

    private final Map<String, AuthenticationProvider> authenticationProviderMap = new HashMap<>();

    private final Map<String, UserDetailsService> userProviderMap = new HashMap<>();

    public ConfigurableProviderManager()
    {
        super(new ArrayList<>(), new EmptyAuthenticationManager());
    }

    public void reload()
        throws IOException
    {
        logger.info("Reloading Authentication.");
        
        authenticationProvidersRegistry.reload();

        reloadAuthenticationItems();
    }

    private void reloadAuthenticationItems()
    {
        authenticationProviderMap.clear();
        authenticationProviderMap.putAll(authenticationProvidersRegistry.getAuthenticationProviderMap());

        userProviderMap.clear();
        userProviderMap.putAll(authenticationProvidersRegistry.getUserDetailsServiceMap());
    }

    @Override
    public List<AuthenticationProvider> getProviders()
    {
        return new ArrayList<>(authenticationProviderMap.values());
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
        for (UserDetailsService userDetailsService : userProviderMap.values())
        {
            try
            {
                externalUserDetails = userDetailsService.loadUserByUsername(username);
                
                break;
            }
            catch (UsernameNotFoundException e)
            {
                continue;
            }
        }

        if (externalUserDetails == null)
        {
            userService.delete(username);

            return null;
        }

        return strongboxUserActualizer.apply(externalUserDetails);
    }

    public void reorder(String first,
                        String second)
        throws IOException
    {
        Map<String, Object> p1 = authenticationProvidersRegistry.getAuthenticationProperties(first);
        Map<String, Object> p2 = authenticationProvidersRegistry.getAuthenticationProperties(second);

        assertValidConfiguration(first, p1);
        assertValidConfiguration(second, p2);

        Integer orderFirst = (Integer) p1.get("order");
        Integer orderSecond = (Integer) p2.get("order");

        p1.put("order", orderSecond);
        p2.put("order", orderFirst);

        boolean result = authenticationProvidersRegistry.mergeProperties()
                                                        .merge(first, p1)
                                                        .merge(second, p2)
                                                        .apply();
        if (!result)
        {
            return;
        }

        reloadAuthenticationItems();
    }

    @Override
    public void updateAuthenticationItems(AuthenticationItems items)
        throws IOException
    {

        MergePropertiesContext mergePropertiesContext = authenticationProvidersRegistry.mergeProperties();
        for (AuthenticationItem item : items.getAuthenticationItemList())
        {
            Map<String, Object> properties = authenticationProvidersRegistry.getAuthenticationProperties(item.getName());

            properties.put("order", item.getOrder());
            properties.put("enabled", Boolean.TRUE.equals(item.getEnabled()));

            mergePropertiesContext = mergePropertiesContext.merge(item.getName(), properties);

        }

        boolean result = mergePropertiesContext.apply();
        if (!result)
        {
            return;
        }

        reloadAuthenticationItems();
    }

    private void assertValidConfiguration(String authenticationItemId,
                                          Map<String, Object> p1)
    {
        if (p1 == null || p1.get("order") == null)
        {
            throw new IllegalArgumentException(
                    String.format("Invalid authentication configuration for [%s]", authenticationItemId));
        }
    }

    @Override
    public AuthenticationItems getAuthenticationItems()
    {
        Map<String, Object> authenticationProperties = authenticationProvidersRegistry.getAuthenticationProperties();

        AuthenticationItems result = new AuthenticationItems();
        result.getAuthenticationItemList()
              .addAll(Stream.concat(authenticationItemStream(AuthenticationProvider.class, authenticationProviderMap,
                                                             authenticationProperties),
                                    authenticationItemStream(UserDetailsService.class, userProviderMap,
                                                             authenticationProperties))
                            .collect(Collectors.toList()));

        return result;
    }

    @Override
    public <T> T getCustomAuthenticationItem(CustomAuthenticationItemMapper<T> mapper)
    {
        Map<String, Object> map = authenticationProvidersRegistry.getAuthenticationProperties(mapper.getConfigurationItemId());

        return mapper.map(map);
    }

    @Override
    public <T> void putCustomAuthenticationItem(T customAuthenticationItem,
                                                CustomAuthenticationItemMapper<T> mapper)
        throws IOException
    {
        String itemId = mapper.getConfigurationItemId();

        authenticationProvidersRegistry.mergeProperties()
                                       .merge(itemId, mapper.map(customAuthenticationItem))
                                       .apply();
    }

    @Override
    public <T> void testCustomAuthenticationItem(T customAuthenticationItem,
                                                 CustomAuthenticationItemMapper<T> mapper,
                                                 Predicate<ApplicationContext> p)
        throws IOException
    {
        String itemId = mapper.getConfigurationItemId();

        authenticationProvidersRegistry.mergeProperties()
                                       .merge(itemId, mapper.map(customAuthenticationItem))
                                       .apply(p);
    }

    private <T> Stream<AuthenticationItem> authenticationItemStream(Class<T> itemClass,
                                                                    Map<String, T> sourceMap,
                                                                    Map<String, Object> authenticationProperties)
    {
        return sourceMap.entrySet()
                        .stream()
                        .map(e -> new AuthenticationItem(
                                e.getKey(),
                                itemClass.getSimpleName(),
                                (Map<String, Object>) authenticationProperties.get(e.getKey())))
                        .sorted((i1,
                                 i2) -> i1.getOrder()
                                          .compareTo(i2.getOrder()));
    }

    @EventListener({ ContextRefreshedEvent.class })
    public void contextRefreshedEvent(ContextRefreshedEvent e)
        throws IOException
    {
        ApplicationContext applicationContext = e.getApplicationContext();
        if (applicationContext == null || applicationContext instanceof AuthenticationConfigurationContext)
        {
            return;
        }

        reload();
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
