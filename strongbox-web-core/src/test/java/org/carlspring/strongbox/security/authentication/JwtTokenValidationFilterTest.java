package org.carlspring.strongbox.security.authentication;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.controllers.support.ErrorResponseEntityBody;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class JwtTokenValidationFilterTest
{

    @Inject
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenValidationFilter jwtTokenValidationFilter;

    @Inject
    private SecurityTokenProvider securityTokenProvider;

    @Test
    public void verifyThatChainGoesDownOnNonExistingTokenInTheRequest()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = mock(MockFilterChain.class);

        jwtTokenValidationFilter.doFilter(request, response, chain);

        verify(chain, times(1)).doFilter(request, response);
    }

    @Test
    public void verifyThatTheResponseReturnsInvalidTokenWhenInvalidTokenHasBeenProvided()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer badToken");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = mock(MockFilterChain.class);

        jwtTokenValidationFilter.doFilter(request, response, chain);
        ErrorResponseEntityBody responseEntityBody = objectMapper.readValue(response.getContentAsByteArray(),
                                                                            ErrorResponseEntityBody.class);

        assertTrue(response.isCommitted());
        assertThat(response.getStatus(), CoreMatchers.equalTo(HttpServletResponse.SC_FORBIDDEN));
        assertThat(response.getContentType(), CoreMatchers.equalTo(MediaType.APPLICATION_JSON_VALUE));
        assertThat(responseEntityBody.getError(), CoreMatchers.equalTo("invalid.token"));
        verify(chain, times(0)).doFilter(request, response);
    }

    @Test
    public void shouldReturnExpiredTokenResponse()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = securityTokenProvider.getToken("przemyslaw", securityTokenProvider.passwordClaimMap("fusik"), 0);
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = mock(MockFilterChain.class);

        jwtTokenValidationFilter.doFilter(request, response, chain);
        ErrorResponseEntityBody responseEntityBody = objectMapper.readValue(response.getContentAsByteArray(),
                                                                            ErrorResponseEntityBody.class);

        assertTrue(response.isCommitted());
        assertThat(response.getStatus(), CoreMatchers.equalTo(HttpServletResponse.SC_FORBIDDEN));
        assertThat(response.getContentType(), CoreMatchers.equalTo(MediaType.APPLICATION_JSON_VALUE));
        assertThat(responseEntityBody.getError(), CoreMatchers.equalTo("expired"));
        verify(chain, times(0)).doFilter(request, response);
    }

    @Test
    public void shouldValidateTokenProperly()
            throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = securityTokenProvider.getToken("przemyslaw", securityTokenProvider.passwordClaimMap("fusik"),
                                                      60);
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = mock(MockFilterChain.class);

        jwtTokenValidationFilter.doFilter(request, response, chain);

        assertFalse(response.isCommitted());
        assertThat(response.getStatus(), CoreMatchers.not(CoreMatchers.equalTo(HttpServletResponse.SC_FORBIDDEN)));
        verify(chain, times(1)).doFilter(request, response);
    }

}
