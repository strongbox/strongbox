package org.carlspring.strongbox.config;

import org.carlspring.strongbox.security.authentication.UnauthorizedEntryPoint;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.RememberMeAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
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

    String TOKEN =           "strongbox-token";

    @Autowired
    private UserDetailsService userDetailService;

    @Autowired
    private RememberMeAuthenticationProvider rememberMeAuthenticationProvider;

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
        auth.userDetailsService(userDetailService)/*.passwordEncoder(passwordEncoder())*/;
        auth.authenticationProvider(rememberMeAuthenticationProvider);
    }

    @Bean
    public RememberMeAuthenticationProvider rememberMeAuthenticationProvider(){
        return new RememberMeAuthenticationProvider(TOKEN);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailService);
        //authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new ShaPasswordEncoder();
    }

    @Bean(name = "unauthorizedEntryPoint")
    AuthenticationEntryPoint unauthorizedEntryPoint()
    {
        UnauthorizedEntryPoint unauthorizedEntryPoint = new UnauthorizedEntryPoint();
        unauthorizedEntryPoint.setRealmName("Strongbox Realm");
        return unauthorizedEntryPoint;
    }
}
