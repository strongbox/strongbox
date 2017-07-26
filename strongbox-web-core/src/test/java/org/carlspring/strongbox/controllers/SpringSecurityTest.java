package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.security.authentication.CustomAnonymousAuthenticationFilter;
import org.carlspring.strongbox.users.domain.User;

import javax.inject.Inject;

import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * @author Alex Oreshkevich
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class SpringSecurityTest
        extends RestAssuredBaseTest
{

    @Inject
    AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    @Test
    @Ignore
    public void testThatAnonymousUserHasFullAccessAccordingToAuthorities()
    {
        anonymousAuthenticationFilter.getAuthorities()
                                     .add(new SimpleGrantedAuthority("VIEW_USER"));

        String url = getContextBaseUrl() + "/users/user/anyName";

        RestAssuredMockMvc.given()
                          .contentType(MediaType.TEXT_PLAIN_VALUE)
                          .when()
                          .get(url)
                          .peek() // Use peek() to print the output
                          .then()
                          .statusCode(HttpStatus.OK.value());
    }

    @Test
    @WithUserDetails("admin")
    public void testUserAuth()
            throws Exception
    {

        String url = getContextBaseUrl() + "/users/greet";

        given().contentType(ContentType.JSON)
               .param("name", "Johan")
               .when()
               .get(url)
               .then()
               .statusCode(200)
               .toString();
    }

    @Test
    public void testUnauthorizedRequest()
    {
        // clear default anonymous authorization context and disable it's population
        ((CustomAnonymousAuthenticationFilter) anonymousAuthenticationFilter).setContextAutoCreationEnabled(false);
        SecurityContextHolder.getContext()
                             .setAuthentication(null);

        RestAssuredMockMvc.given()
                          .contentType(MediaType.TEXT_PLAIN_VALUE)
                          .param("name", "John")
                          .when()
                          .get(getContextBaseUrl() + "/users/greet")
                          .peek() // Use peek() to print the output
                          .then()
                          .statusCode(UNAUTHORIZED.value());
    }

    @Test
    @Ignore
    public void testJWTAuth()
    {
        String url = getContextBaseUrl() + "/users/user/authenticate";

        String basicAuth = "Basic YWRtaW46cGFzc3dvcmQ=";
        logger.info(String.format("Get JWT Token with Basic Authentication: user-[%s]; auth-[%s]", "admin",
                                  basicAuth));
        String token = given().contentType(ContentType.JSON)
                              .header("Authorization", basicAuth)
                              .when()
                              .get(url)
                              .then()
                              .statusCode(200)
                              .extract()
                              .asString();

        logger.info(String.format("Gereet with Basic Authentication: user-[%s]; auth-[%s]", "admin",
                                  basicAuth));
        url = getContextBaseUrl() + "/users/greet";
        given().contentType(ContentType.JSON)
               .header("Authorization", basicAuth)
               .when()
               .get(url)
               .then()
               .statusCode(401);

        logger.info(String.format("Gereet with JWT Authentication: user-[%s]; token-[%s]", "admin",
                                  token));
        given().contentType(ContentType.JSON)
               .header("Authorization", String.format("Bearer %s", token))
               .when()
               .get(url)
               .then()
               .statusCode(200);
    }

    @Test
    @Ignore
    public void testJWTExpire()
            throws InterruptedException
    {
        String url = getContextBaseUrl() + "/users/user/authenticate";

        String basicAuth = "Basic YWRtaW46cGFzc3dvcmQ=";
        logger.info(String.format("Get JWT Token with Basic Authentication: user-[%s]; auth-[%s]", "admin",
                                  basicAuth));
        String token = given().contentType(ContentType.JSON)
                              .header("Authorization", basicAuth)
                              .when()
                              .get(url + String.format("?expireSeconds=%s", 3))
                              .then()
                              .statusCode(200)
                              .extract()
                              .asString();

        logger.info(String.format("Gereet with JWT Authentication: user-[%s]; token-[%s]", "admin",
                                  token));
        url = getContextBaseUrl() + "/users/greet";
        given().contentType(ContentType.JSON)
               .header("Authorization", String.format("Bearer %s", token))
               .when()
               .get(url)
               .then()
               .statusCode(200);

        Thread.sleep(3500);
        logger.info(String.format("Check JWT Authentication expired: user-[%s]; token-[%s]", "admin",
                                  token));
        given().contentType(ContentType.JSON)
               .header("Authorization", String.format("Bearer %s", token))
               .when()
               .get(url)
               .then()
               .statusCode(401);
    }

    @Test
    @WithUserDetails("user")
    public void testThatUserHasViewUsersPrivilege()
    {
        String userName = "user";
        given().contentType("application/json")
               .when()
               .get("/users/user/" + userName)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    @Test
    @WithUserDetails("deployer")
    public void testThatNewUserCreationIsForbiddenForCertainUser()
    {
        User user = new User();
        user.setUsername("someNewUserName");
        given().contentType("application/json")
               .body(user)
               .when()
               .put("/users/user")
               .peek()
               .then()
               .body("error", CoreMatchers.equalTo("unauthorized"))
               .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
