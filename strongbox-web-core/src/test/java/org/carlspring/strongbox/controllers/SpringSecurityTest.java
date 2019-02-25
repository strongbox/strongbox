package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.security.authentication.CustomAnonymousAuthenticationFilter;

import javax.inject.Inject;

import io.restassured.http.ContentType;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@IntegrationTest
public class SpringSecurityTest
        extends RestAssuredBaseTest
{

    @Inject
    private AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    @Override
    @BeforeEach
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

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
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
               .body("error", CoreMatchers.equalTo("forbidden"))
               .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
