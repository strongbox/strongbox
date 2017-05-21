package org.carlspring.strongbox.config;

import org.carlspring.strongbox.authentication.config.AuthenticationConfig;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.security.authentication.CustomAnonymousAuthenticationFilter;
import org.carlspring.strongbox.security.authentication.StrongboxAuthenticationFilter;
import org.carlspring.strongbox.security.authentication.suppliers.AuthenticationSupplier;
import org.carlspring.strongbox.security.authentication.suppliers.AuthenticationSuppliers;
import org.carlspring.strongbox.security.vote.MethodAccessDecisionManager;
import org.carlspring.strongbox.users.security.AuthoritiesProvider;

import javax.inject.Inject;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Import(AuthenticationConfig.class)
@Configuration
@EnableWebSecurity
public class SecurityConfig
        extends WebSecurityConfigurerAdapter
{

    @Inject
    private AuthoritiesProvider authoritiesProvider;

    @Inject
    private AuthenticatorsRegistry authenticatorsRegistry;

    @Inject
    private List<AuthenticationSupplier> suppliers;

    @Override
    protected void configure(HttpSecurity http)
            throws Exception
    {
        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .anonymous()
            .authenticationFilter(anonymousAuthenticationFilter())
            .and()
            .csrf()
            .disable();

        http.addFilterBefore(strongboxAuthenticationFilter(),
                             BasicAuthenticationFilter.class);
    }

    @Bean
    StrongboxAuthenticationFilter strongboxAuthenticationFilter()
    {
        return new StrongboxAuthenticationFilter(new AuthenticationSuppliers(suppliers), authenticatorsRegistry);
    }

    @Bean
    AnonymousAuthenticationFilter anonymousAuthenticationFilter()
    {
        List<GrantedAuthority> anonymousRoles = AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS");
        anonymousRoles.addAll(authoritiesProvider.getAuthoritiesByRoleName("ANONYMOUS_ROLE"));

        return new CustomAnonymousAuthenticationFilter("strongbox-unique-key",
                                                       "anonymousUser",
                                                       anonymousRoles);
    }

    /**
     * This Configuration enables @PreAuthorize annotations
     *
     * @author Sergey Bespalov
     */
    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public static class MethodSecurityConfig
            extends GlobalMethodSecurityConfiguration
    {

        @Inject
        MethodAccessDecisionManager methodAccessDecisionManager;

        @Override
        protected AccessDecisionManager accessDecisionManager()
        {
            return methodAccessDecisionManager;
        }

    }

}