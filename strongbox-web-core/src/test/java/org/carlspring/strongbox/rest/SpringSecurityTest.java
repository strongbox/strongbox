package org.carlspring.strongbox.rest;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import javax.inject.Inject;

import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.security.authentication.CustomAnonymousAuthenticationFilter;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;

/**
 * @author Alex Oreshkevich
 */
//@IntegrationTest
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
@Rollback
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
        anonymousAuthenticationFilter.getAuthorities().add(new SimpleGrantedAuthority("VIEW_USER"));

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
        ((CustomAnonymousAuthenticationFilter) anonymousAuthenticationFilter).setEnableContextAutoCreation(false);
        SecurityContextHolder.getContext().setAuthentication(null);

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

}
