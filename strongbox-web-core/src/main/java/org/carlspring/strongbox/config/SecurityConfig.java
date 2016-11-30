package org.carlspring.strongbox.config;

import javax.annotation.PostConstruct;

import org.carlspring.strongbox.security.authentication.CustomAnonymousAuthenticationFilter;
import org.carlspring.strongbox.security.authentication.UnauthorizedEntryPoint;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig
        extends WebSecurityConfigurerAdapter
{

    @Autowired
    private AuthorizationConfigProvider authorizationConfigProvider;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    private AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    public SecurityConfig()
    {
        super();

        anonymousAuthenticationFilter = new CustomAnonymousAuthenticationFilter("strongbox-unique-key", "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    }

    @PostConstruct
    public void init()
    {
        authorizationConfigProvider.getConfig().ifPresent(
                (config) -> {
                    anonymousAuthenticationFilter.getAuthorities().addAll(config.getAnonymousAuthorities());
                });
    }

    @Bean
    public AnonymousAuthenticationFilter anonymousAuthenticationFilter()
    {
        return anonymousAuthenticationFilter;
    }

    @Override
    protected void configure(HttpSecurity http)
                                                throws Exception
    {
        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            .and()
            .authorizeRequests()
            .antMatchers("/docs/**", "/assets/**")
            .permitAll()
            .and()
            .anonymous()
            .authenticationFilter(anonymousAuthenticationFilter)
            .and()
            .authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(unauthorizedEntryPoint())
            .and()
            .httpBasic()
            .and()
            .csrf()
            .disable()
            .formLogin()
            .and()
            .logout()
            .logoutUrl("/logout");
    }

    public void configure(AuthenticationManagerBuilder auth)
                                                             throws Exception
    {
        auth.authenticationProvider(authenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean(name = "unauthorizedEntryPoint")
    AuthenticationEntryPoint unauthorizedEntryPoint()
    {
        UnauthorizedEntryPoint unauthorizedEntryPoint = new UnauthorizedEntryPoint();
        unauthorizedEntryPoint.setRealmName("Strongbox Realm");

        return unauthorizedEntryPoint;
    }

}
