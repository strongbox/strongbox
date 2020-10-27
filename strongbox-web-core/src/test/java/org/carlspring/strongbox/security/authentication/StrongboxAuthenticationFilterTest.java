package org.carlspring.strongbox.security.authentication;

import org.carlspring.strongbox.security.authentication.strategy.DelegatingAuthenticationConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

class StrongboxAuthenticationFilterTest
{

    private DelegatingAuthenticationConverter delegatingAuthenticationConverter;
    private AuthenticationManager authenticationManager;

    private HttpServletRequest httpServletRequest;
    private HttpServletResponse httpServletResponse;
    private FilterChain filterChain;

    private StrongboxAuthenticationFilter strongboxAuthenticationFilter;

    @BeforeEach
    void setup()
    {
        delegatingAuthenticationConverter = Mockito.mock(DelegatingAuthenticationConverter.class);
        authenticationManager = Mockito.mock(AuthenticationManager.class);

        httpServletRequest = Mockito.mock(HttpServletRequest.class);
        httpServletResponse = Mockito.mock(HttpServletResponse.class);
        filterChain = Mockito.mock(FilterChain.class);

        strongboxAuthenticationFilter = new StrongboxAuthenticationFilter(delegatingAuthenticationConverter, authenticationManager);
    }

    @Test
    void authenticationIsNullTest() throws ServletException, IOException
    {
        Mockito.when(delegatingAuthenticationConverter.convert(httpServletRequest)).thenReturn(null);

        strongboxAuthenticationFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        Mockito.verify(filterChain).doFilter(httpServletRequest, httpServletResponse);

        Mockito.verifyNoInteractions(authenticationManager);
    }

    @Test
    void authenticationResultIsAuthenticatedTest() throws ServletException, IOException
    {
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.isAuthenticated()).thenReturn(true);

        Mockito.when(delegatingAuthenticationConverter.convert(httpServletRequest)).thenReturn(authentication);

        strongboxAuthenticationFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        Mockito.verify(filterChain).doFilter(httpServletRequest, httpServletResponse);

        Mockito.verifyNoInteractions(authenticationManager);
    }

    @Test
    void authenticationResultIsNotAuthenticatedTest() throws ServletException, IOException
    {
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.isAuthenticated()).thenReturn(false);

        Mockito.when(delegatingAuthenticationConverter.convert(httpServletRequest)).thenReturn(authentication);

        strongboxAuthenticationFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        Mockito.verify(authenticationManager).authenticate(authentication);

        Mockito.verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
    }
}
