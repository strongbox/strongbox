package org.carlspring.strongbox.config;

import org.carlspring.strongbox.security.authentication.CustomAnonymousAuthenticationFilter;
import org.carlspring.strongbox.security.authentication.UnauthorizedEntryPoint;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;

import javax.annotation.PostConstruct;

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
    AuthorizationConfigProvider authorizationConfigProvider;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Autowired
    private AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    @Override
    protected void configure(HttpSecurity http)
            throws Exception
    {
        http.sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
            .and()
            .exceptionHandling()
            .authenticationEntryPoint(unauthorizedEntryPoint())
            .and()
            .httpBasic()
            .and()
            .csrf().disable()
            .formLogin()
            .and()
            .authorizeRequests()
            .antMatchers("/docs/**", "/assets/**").permitAll()
            .and()
            .anonymous().authenticationFilter(anonymousAuthenticationFilter)
            .and()
            .logout()
            .logoutUrl("/logout");
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth)
            throws Exception
    {
        auth.authenticationProvider(authenticationProvider);
    }

    @PostConstruct
    public void init()
    {
        // load anonymous user privileges from database
        authorizationConfigProvider.getConfig().ifPresent(
                config -> anonymousAuthenticationFilter.getAuthorities().addAll(config.getAnonymousAuthorities())
        );
    }

    @Bean
    public AnonymousAuthenticationFilter customAnonymousAuthenticationFilter()
    {
        return new CustomAnonymousAuthenticationFilter("strongbox-unique-key", "anonymousUser",
                                                       AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
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
