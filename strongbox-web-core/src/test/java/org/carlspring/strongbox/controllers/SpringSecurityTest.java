package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.security.authentication.CustomAnonymousAuthenticationFilter;

import javax.inject.Inject;

import io.restassured.http.ContentType;
import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class SpringSecurityTest
        extends RestAssuredBaseTest
{

    @Inject
    private AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    @Override
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/users");
    }

    @Test
    public void testThatAnonymousUserHasUserViewAccessAccordingToAuthorities()
    {
        anonymousAuthenticationFilter.getAuthorities().add(new SimpleGrantedAuthority("VIEW_USER"));

        final String username = "admin";

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(getContextBaseUrl() + "/{username}", username)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(username));
    }

    @Test
    public void testUnauthorizedRequest()
    {
        // clear default anonymous authorization context and disable it's population
        ((CustomAnonymousAuthenticationFilter) anonymousAuthenticationFilter).setContextAutoCreationEnabled(false);
        SecurityContextHolder.getContext()
                             .setAuthentication(null);

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @Ignore
    public void testJWTAuth()
    {
        // TODO: Rewrite this test case.
        String url = getContextBaseUrl() + "/api/users/user/authenticate";

        String basicAuth = "Basic YWRtaW46cGFzc3dvcmQ=";
        logger.info(String.format("Get JWT Token with Basic Authentication: user-[%s]; auth-[%s]", "admin",
                                  basicAuth));
        String token = given().contentType(ContentType.JSON)
                              .header(HttpHeaders.AUTHORIZATION, basicAuth)
                              .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                              .when()
                              .get(url)
                              .then()
                              .statusCode(HttpStatus.OK.value())
                              .extract()
                              .asString();

        logger.info(String.format("Greet with Basic Authentication: user-[%s]; auth-[%s]", "admin",
                                  basicAuth));
        url = getContextBaseUrl() + "/users/greet";
        given().header(HttpHeaders.AUTHORIZATION, basicAuth)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value());

        logger.info(String.format("Greet with JWT Authentication: user-[%s]; token-[%s]", "admin",
                                  token));
        given().header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token))
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("token", equalTo(token));
    }

    @Test
    @Ignore
    public void testJWTExpire()
            throws InterruptedException
    {
        // TODO: Rewrite this test case.
        String url = getContextBaseUrl() + "/api/users/user/authenticate";

        String basicAuth = "Basic YWRtaW46cGFzc3dvcmQ=";
        logger.info(String.format("Get JWT Token with Basic Authentication: user-[%s]; auth-[%s]", "admin",
                                  basicAuth));
        String token = given().header(HttpHeaders.AUTHORIZATION, basicAuth)
                              .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                              .when()
                              .get(url + String.format("?expireSeconds=%s", 3))
                              .then()
                              .statusCode(HttpStatus.OK.value())
                              .extract()
                              .asString();

        logger.info(String.format("Greet with JWT Authentication: user-[%s]; token-[%s]", "admin",
                                  token));
        url = getContextBaseUrl() + "/api/users/greet";
        given().header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token))
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value());

        Thread.sleep(3500);
        logger.info(String.format("Check JWT Authentication expired: user-[%s]; token-[%s]", "admin",
                                  token));
        given().header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", token))
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @WithUserDetails("deployer")
    public void testThatNewUserCreationIsForbiddenForCertainUser()
    {
        UserForm user = new UserForm();
        user.setUsername("someNewUserName");
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(user)
               .when()
               .put(getContextBaseUrl())
               .peek()
               .then()
               .body("error", CoreMatchers.equalTo("Access is denied"))
               .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
