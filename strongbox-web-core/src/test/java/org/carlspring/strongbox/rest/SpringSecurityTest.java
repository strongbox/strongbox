package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.security.authentication.CustomAnonymousAuthenticationFilter;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

/**
 * @author Alex Oreshkevich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
public class SpringSecurityTest
        extends BackendBaseTest
{

    @Autowired
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
    //@WithMockUser(username = "user", password = "password321")
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
    public void testUrlAsPathVariable()
    {
        String someUrl = "org/foo/bar/metadata/metadata-foo/3.1-SNAPSHOT/metadata-foo-3.1-20161017.182007-1.jar";

        // storage0     -> storageId
        // snapshots    -> repositoryId
        String requestUrl = getContextBaseUrl() + "/test/storage0/snapshots/" + someUrl;

        RestAssuredMockMvc.given()
                          .contentType(MediaType.TEXT_PLAIN_VALUE)
                          .when()
                          .get(requestUrl)
                          .peek() // Use peek() to print the output
                          .then()
                          .statusCode(HttpStatus.OK.value());
    }
}
