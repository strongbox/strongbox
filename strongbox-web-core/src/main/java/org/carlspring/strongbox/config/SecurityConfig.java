package org.carlspring.strongbox.config;

import org.carlspring.strongbox.security.authentication.CustomAnonymousAuthenticationFilter;
import org.carlspring.strongbox.security.authentication.Http401AuthenticationEntryPoint;
import org.carlspring.strongbox.security.authentication.JWTAuthenticationFilter;
import org.carlspring.strongbox.security.authentication.JWtAuthenticationProvider;
import org.carlspring.strongbox.users.security.AuthorizationConfigProvider;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig
        extends WebSecurityConfigurerAdapter
{

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth,
                                @Qualifier("userDetailsAuthenticationProvider")
                                        AuthenticationProvider userDetailsAuthenticationProvider,
                                @Qualifier("jwtAuthenticationProvider")
                                        AuthenticationProvider jwtAuthenticationProvider)
    {
        auth.authenticationProvider(userDetailsAuthenticationProvider)
            .authenticationProvider(jwtAuthenticationProvider)
            .eraseCredentials(false);

    }

    /**
     * This configuration specifies BasicAuthentication rules. Such authentication triggered first and only for concrete
     * set of URL patterns. All the other authentication configured in {@link JwtSecurityConfig}
     *
     * @author Sergey Bespalov
     */
    @Configuration
    @Order(1)
    public static class BasicSecurityConfig
            extends WebSecurityConfigurerAdapter
    {

        @Override
        protected void configure(HttpSecurity http)
                throws Exception
        {
            http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .requestMatchers()
                .antMatchers("/users/user/authenticate", "/storages")
                .and()
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .httpBasic()
                .and()
                .csrf()
                .disable();
        }
    }

    /**
     * This configuration specifies JWTAuthentication and Anonymous access rules. It triggered after
     * {@link BasicSecurityConfig} and authenticate unauthorized requests if needed.
     *
     * @author Sergey Bespalov
     *
     */
    @Configuration
    @Order(2)
    public static class JwtSecurityConfig
            extends WebSecurityConfigurerAdapter
    {

        @Autowired
        private AuthorizationConfigProvider authorizationConfigProvider;
        private AnonymousAuthenticationFilter anonymousAuthenticationFilter;

        public JwtSecurityConfig()
        {
            anonymousAuthenticationFilter = new CustomAnonymousAuthenticationFilter("strongbox-unique-key",
                                                                                    "anonymousUser",
                                                                                    AuthorityUtils.createAuthorityList(
                                                                                            "ROLE_ANONYMOUS"));
        }

        @PostConstruct
        public void init()
        {
            authorizationConfigProvider.getConfig()
                                       .ifPresent((config) ->
                                                  {
                                                      anonymousAuthenticationFilter.getAuthorities()
                                                                                   .addAll(config.getAnonymousAuthorities());
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

            JWTAuthenticationFilter jwtFilter = new JWTAuthenticationFilter(authenticationManager());

            http.sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/docs/**", "/assets/**")
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .addFilterAfter(jwtFilter, BasicAuthenticationFilter.class)
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/")
                .and()
                .anonymous()
                .authenticationFilter(anonymousAuthenticationFilter)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new Http401AuthenticationEntryPoint("Bearer"))
                .and()
                .csrf()
                .disable();
        }

    }

    @Bean
    public AuthenticationProvider userDetailsAuthenticationProvider(UserDetailsService userDetailsService)
    {
        DaoAuthenticationProvider result = new DaoAuthenticationProvider();
        result.setUserDetailsService(userDetailsService);
        return result;
    }

    @Bean
    public AuthenticationProvider jwtAuthenticationProvider()
    {
        return new JWtAuthenticationProvider();
    }
}