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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.containsString;
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

}
