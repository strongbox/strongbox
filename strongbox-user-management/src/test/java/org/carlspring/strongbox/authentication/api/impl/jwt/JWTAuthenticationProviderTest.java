package org.carlspring.strongbox.authentication.api.impl.jwt;

import org.carlspring.strongbox.users.security.SecurityTokenProvider;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.Arrays;
import org.hamcrest.CoreMatchers;
import org.jose4j.lang.JoseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * @author Przemyslaw Fusik
 */
public class JWTAuthenticationProviderTest
{

    private AuthenticationProvider jwtAuthenticationProvider;

    private SecurityTokenProvider securityTokenProvider;

    private UserDetails userDetails;

    @Before
    public void setup()
            throws UnsupportedEncodingException
    {
        securityTokenProvider = new SecurityTokenProvider();
        securityTokenProvider.init("fusik");

        userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getAuthorities())
               .thenReturn(Arrays.asList(new GrantedAuthority[]{ new SimpleGrantedAuthority("ROLE_DEVELOPER") }));
        Mockito.when(userDetails.getUsername())
               .thenReturn("username");
        Mockito.when(userDetails.getPassword())
               .thenReturn("secretPassword");
        Mockito.when(userDetails.isAccountNonLocked())
               .thenReturn(true);
        Mockito.when(userDetails.isEnabled())
               .thenReturn(true);
        Mockito.when(userDetails.isCredentialsNonExpired())
               .thenReturn(true);
        Mockito.when(userDetails.isAccountNonExpired())
               .thenReturn(true);

        UserDetailsService userDetailsService = Mockito.mock(UserDetailsService.class);
        Mockito.when(userDetailsService.loadUserByUsername(Matchers.anyString()))
               .thenReturn(userDetails);

        jwtAuthenticationProvider = new JWTAuthenticationProvider(userDetailsService, securityTokenProvider);
    }

    @Test
    public void shouldReturnExpectedAuthentication()
            throws JoseException
    {

        Map<String, String> claimMap = new HashMap<>();
        claimMap.put("credentials", "secretPassword");
        String token = securityTokenProvider.getToken("username", claimMap, null);

        Authentication authentication = jwtAuthenticationProvider.authenticate(new JWTAuthentication(token));

        Assert.assertThat(authentication, org.hamcrest.Matchers.notNullValue());
        Assert.assertThat((UsernamePasswordAuthenticationToken) authentication,
                          CoreMatchers.isA(UsernamePasswordAuthenticationToken.class));
        Assert.assertThat(authentication.getPrincipal(), CoreMatchers.equalTo(userDetails));
        Assert.assertThat(authentication.getCredentials(), CoreMatchers.equalTo("secretPassword"));
        Assert.assertThat(authentication.getAuthorities(), CoreMatchers.equalTo(
                Arrays.asList(new GrantedAuthority[]{ new SimpleGrantedAuthority("ROLE_DEVELOPER") })));
    }

}