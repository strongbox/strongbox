package org.carlspring.strongbox.config;

import org.carlspring.strongbox.authentication.config.AuthenticationConfig;
import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.security.CustomAccessDeniedHandler;
import org.carlspring.strongbox.security.authentication.CustomAnonymousAuthenticationFilter;
import org.carlspring.strongbox.security.authentication.Http401AuthenticationEntryPoint;
import org.carlspring.strongbox.security.authentication.JwtTokenValidationFilter;
import org.carlspring.strongbox.security.authentication.StrongboxAuthenticationFilter;
import org.carlspring.strongbox.security.authentication.suppliers.AuthenticationSupplier;
import org.carlspring.strongbox.security.authentication.suppliers.AuthenticationSuppliers;
import org.carlspring.strongbox.security.vote.MethodAccessDecisionManager;
import org.carlspring.strongbox.users.security.AuthoritiesProvider;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import static org.carlspring.strongbox.authorization.service.AuthorizationConfigService.ANONYMOUS_ROLE;

@ComponentScan({ "org.carlspring.strongbox.security" })
@Import({ DataServiceConfig.class,
          EventsConfig.class,
          UsersConfig.class,
          AuthenticationConfig.class})
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
    public void init(WebSecurity web)
            throws Exception
    {
        super.init(web);
        DefaultHttpFirewall httpFirewall = new DefaultHttpFirewall();
        httpFirewall.setAllowUrlEncodedSlash(true);
        web.httpFirewall(httpFirewall);
    }

    @Override
    protected void configure(HttpSecurity http)
            throws Exception
    {
        http.addFilterBefore(strongboxAuthenticationFilter(),
                             BasicAuthenticationFilter.class)
            .addFilterBefore(jwtTokenValidationFilter(), StrongboxAuthenticationFilter.class)
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .exceptionHandling()
            .accessDeniedHandler(accessDeniedHandler())
            // TODO SB-813
            .authenticationEntryPoint(customBasicAuthenticationEntryPoint())
            .and()
            .anonymous()
            .authenticationFilter(anonymousAuthenticationFilter())
            .and()
            .cors()
            .and()
            .csrf()
            .disable();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource()
    {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(Arrays.asList("GET",
                                                      "PUT",
                                                      "POST",
                                                      "DELETE",
                                                      "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Accept",
                                                      "Accepts",
                                                      "Authorization",
                                                      "Access-Control-Allow-Headers",
                                                      "Access-Control-Request-Headers",
                                                      "Access-Control-Request-Method",
                                                      "DNT",
                                                      "Keep-Alive",
                                                      "User-Agent",
                                                      "X-Requested-With",
                                                      "If-Modified-Since",
                                                      "Cache-Control",
                                                      "Content-Type",
                                                      "Content-Range,Range"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(600L);

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    AccessDeniedHandler accessDeniedHandler()
    {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    AuthenticationEntryPoint customBasicAuthenticationEntryPoint()
    {
        return new Http401AuthenticationEntryPoint("Strongbox Repository Manager");
    }

    @Bean
    StrongboxAuthenticationFilter strongboxAuthenticationFilter()
    {
        return new StrongboxAuthenticationFilter(new AuthenticationSuppliers(suppliers), authenticatorsRegistry);
    }

    @Bean
    JwtTokenValidationFilter jwtTokenValidationFilter()
    {
        return new JwtTokenValidationFilter();
    }

    @Bean
    AnonymousAuthenticationFilter anonymousAuthenticationFilter()
    {
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS");
        authorities.addAll(authoritiesProvider.getAuthoritiesByRoleName(ANONYMOUS_ROLE));

        return new CustomAnonymousAuthenticationFilter("strongbox-unique-key",
                                                       "anonymousUser",
                                                       authorities);
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
