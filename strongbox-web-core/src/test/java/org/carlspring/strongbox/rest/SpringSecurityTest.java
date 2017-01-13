package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;
import org.carlspring.strongbox.security.authentication.CustomAnonymousAuthenticationFilter;

import javax.inject.Inject;

import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.springframework.http.HttpStatus.OK;
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
        clearAuthenticationContext();

        RestAssuredMockMvc.given()
                          .contentType(MediaType.TEXT_PLAIN_VALUE)
                          .param("name", "John")
                          .when()
                          .get(getContextBaseUrl() + "/users/greet")
                          .peek() // Use peek() to print the output
                          .then()
                          .statusCode(UNAUTHORIZED.value());
    }

    // checks that paths like http://localhost:48080/storages/storage0/releases/.index/** are accessible
    // server should respond with either 404 or 200 for such kind of requests
    @Test
    @WithAnonymousUser
    public void testRequestForIndexData() throws Exception {

        // http://localhost:48080/storages/storage0/releases/.index/nexus-maven-repository-index.properties
        final String url =
                getContextBaseUrl() + "/storages/storage0/releases/.index/nexus-maven-repository-index.properties";
        logger.info("Calling URL " + url);

        given().header("user-agent", "Maven/*")
               .contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(OK.value());
    }

    private void clearAuthenticationContext(){

        // clear default anonymous authorization context and disable it's population
        ((CustomAnonymousAuthenticationFilter) anonymousAuthenticationFilter).setEnableContextAutoCreation(false);
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}
