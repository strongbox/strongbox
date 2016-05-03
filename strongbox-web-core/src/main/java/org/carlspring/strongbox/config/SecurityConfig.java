package org.carlspring.strongbox.config;

import org.carlspring.strongbox.rest.app.spring.security.StrongboxUserDetailService;
import org.carlspring.strongbox.rest.app.spring.security.UnauthorizedEntryPoint;
import org.carlspring.strongbox.rest.app.spring.security.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.annotation.Resource;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter
{

    @Resource(name = "strongboxUserDetailService")
    private StrongboxUserDetailService strongboxUserDetailService;

    @Override
    protected void configure(HttpSecurity http) throws Exception
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
                .logoutUrl("/logout")
                .and()
        .authorizeRequests()
                .antMatchers("/configuration/strongbox/**").hasRole("ADMIN")
                .antMatchers("/**").permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception
    {
        auth.userDetailsService(strongboxUserDetailService)
                .passwordEncoder(passwordEncoder())
                .and()
                .inMemoryAuthentication()
                .withUser("maven").password("password").roles("USER");
    }

    @Bean(name = "userDao")
    UserRepository userDao()
    {
        return  new UserRepository();
    }

    @Bean(name = "userDetailsService")
    @Override
    public UserDetailsService userDetailsService()
    {
        return new StrongboxUserDetailService();
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
