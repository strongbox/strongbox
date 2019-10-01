package org.carlspring.strongbox.controllers;


import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.controllers.login.LoginOutput;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.security.JwtAuthenticationClaimsProvider.JwtAuthentication;
import org.carlspring.strongbox.users.security.JwtClaimsProvider;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.carlspring.strongbox.users.userdetails.SpringSecurityUser;

import javax.inject.Inject;
import java.util.Collections;

import org.jose4j.jwt.NumericDate;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.TestSecurityContextHolder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.security.authentication.JwtTokenFetcher.AUTHORIZATION_COOKIE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author adavid9
 * @author Pablo Tirado
 */
@IntegrationTest
public class JwtAuthenticationTest
        extends RestAssuredBaseTest
{
    @Inject
    private SecurityTokenProvider securityTokenProvider;

    @Inject
    @JwtAuthentication
    private JwtClaimsProvider jwtClaimsProvider;

    @Inject
    private UserDetailsService userDetailsService;

    @Override
    @BeforeEach
    public void init()
        throws Exception
    {
        super.init();

        setContextBaseUrl(getContextBaseUrl() + "/api");
        TestSecurityContextHolder.clearContext();
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testJWTAuthShouldPassWithToken()
        throws Exception
    {
        String url = getContextBaseUrl() + "/users";

        String basicAuth = "Basic YWRtaW46cGFzc3dvcmQ=";

        String body = mockMvc.header(HttpHeaders.AUTHORIZATION, basicAuth)
                             .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                             .when()
                             .get(getContextBaseUrl() + "/login")
                             .then()
                             .statusCode(HttpStatus.OK.value())
                             .extract()
                             .asString();
        TestSecurityContextHolder.clearContext();
        SecurityContextHolder.clearContext();

        mockMvc.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body(notNullValue());
        TestSecurityContextHolder.clearContext();
        SecurityContextHolder.clearContext();

        // this token will expire after 1 hour
        String tokenValue = getTokenValue(body);
        mockMvc.header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(tokenValue))
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(notNullValue());
    }

    @Test
    public void testJWTAuthShouldFailWithoutToken()
    {
        String decodedErrorMessage = getI18nInsufficientAuthenticationErrorMessage();

        String url = getContextBaseUrl() + "/users";

        mockMvc.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo(decodedErrorMessage));
    }

    @Test
    public void testJWTInvalidToken()
    {
        String url = getContextBaseUrl() + "/users";

        String invalid_token = "ABCD";

        mockMvc.header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(invalid_token))
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo("invalid.token"));
    }

    @Test
    public void testJWTExpirationToken()
        throws Exception
    {
        String url = getContextBaseUrl() + "/users";

        // create token that will expire after 1 second
        SpringSecurityUser userDetails = (SpringSecurityUser) userDetailsService.loadUserByUsername("admin");
        String expiredToken = securityTokenProvider.getToken(userDetails.getUsername(),
                                                             jwtClaimsProvider.getClaims(userDetails), 3, null);

        mockMvc.header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(expiredToken))
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(notNullValue());

        Thread.sleep(3000);

        mockMvc.header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(expiredToken))
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo("expired"));
    }

    @Test
    public void testJWTIssuedAtFuture()
        throws Exception
    {
        String url = getContextBaseUrl() + "/users";

        NumericDate futureNumericDate = NumericDate.now();
        // add five minutes to the current time to create a JWT issued in the
        // future
        futureNumericDate.addSeconds(300);

        String token = securityTokenProvider.getToken("admin", Collections.emptyMap(), 10, futureNumericDate);

        mockMvc.header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(token))
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo("invalid.token"));
    }

    @Test
    public void testJWTAuthWithCookieShouldPass()
    {

        String url = getContextBaseUrl() + "/users";
        String basicAuth = "Basic YWRtaW46cGFzc3dvcmQ=";

        LoginOutput body = mockMvc.header(HttpHeaders.AUTHORIZATION, basicAuth)
                                  .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                                  .when()
                                  .get(getContextBaseUrl() + "/login")
                                  .then()
                                  .statusCode(HttpStatus.OK.value())
                                  .extract()
                                  .as(LoginOutput.class);
        TestSecurityContextHolder.clearContext();
        SecurityContextHolder.clearContext();

        assertThat(body).isNotNull();
        assertThat(body.getToken()).isNotNull();

        mockMvc.cookie(AUTHORIZATION_COOKIE, body.getToken())
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(notNullValue());
    }

    private String getTokenValue(String body)
        throws Exception
    {
        JSONObject extractToken = new JSONObject(body);
        return extractToken.getString("token");
    }

    private String getAuthorizationHeader(String tokenValue)
    {
        return String.format("Bearer %s", tokenValue);
    }

}
