package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.TestSecurityContextHolder;

import javax.inject.Inject;
import java.util.Collections;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author: adavid9
 */
@IntegrationTest
public class JwtAuthenticationTest
        extends RestAssuredBaseTest
{

    private static final String UNAUTHORIZED_MESSAGE = "Full authentication is required to access this resource";

    @Inject
    private SecurityTokenProvider securityTokenProvider;

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

        String body = given().header(HttpHeaders.AUTHORIZATION, basicAuth)
                             .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                             .when()
                             .get(getContextBaseUrl() + "/login")
                             .then()
                             .statusCode(HttpStatus.OK.value())
                             .extract()
                             .asString();
        TestSecurityContextHolder.clearContext();
        SecurityContextHolder.clearContext();

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body(notNullValue());
        TestSecurityContextHolder.clearContext();
        SecurityContextHolder.clearContext();
        
        // this token will expire after 1 hour
        String tokenValue = getTokenValue(body);
        given().header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(tokenValue))
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

        String url = getContextBaseUrl() + "/users";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo(UNAUTHORIZED_MESSAGE));
    }

    @Test
    public void testJWTExpirationToken()
            throws Exception
    {

        String url = getContextBaseUrl() + "/users";

        // generate token that will expire after 1 second
        String expiredToken = securityTokenProvider.getToken("admin", Collections.emptyMap(), 3);

        given().header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(expiredToken))
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(notNullValue());

        Thread.sleep(3000);

        given().header(HttpHeaders.AUTHORIZATION, getAuthorizationHeader(expiredToken))
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo("expired"));
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
