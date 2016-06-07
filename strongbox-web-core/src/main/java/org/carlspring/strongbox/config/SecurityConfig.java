package org.carlspring.strongbox.config;

import org.carlspring.strongbox.security.authentication.UnauthorizedEntryPoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * Configures annotation-based security for every method based on Spring Secured annotation.
 *
 * @see {@linkplain http://docs.spring.io/autorepo/docs/spring-security/4.1.x/reference/htmlsingle/#enableglobalmethodsecurity}
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig
        extends WebSecurityConfigurerAdapter
{

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Override
    protected void configure(HttpSecurity http)
            throws Exception
    {
        http
                .sessionManagement()
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
                .logout()
                .logoutUrl("/logout");
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder auth)
            throws Exception
    {
        auth.authenticationProvider(authenticationProvider);
        /*auth
                .inMemoryAuthentication()
                .withUser("maven").password("password").roles(Roles.all())
                .and()
                .withUser("admin").password("password").roles(Roles.all()); */
    }

    @Bean
    PasswordEncoder passwordEncoder()
    {
        return new StandardPasswordEncoder("ThisIsASecretSoChangeMe");
    }

    @Bean(name = "unauthorizedEntryPoint")
    AuthenticationEntryPoint unauthorizedEntryPoint()
    {
        UnauthorizedEntryPoint unauthorizedEntryPoint = new UnauthorizedEntryPoint();
        unauthorizedEntryPoint.setRealmName("Strongbox Realm");
        return unauthorizedEntryPoint;
    }
}
