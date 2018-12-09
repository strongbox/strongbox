package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;


/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@IntegrationTest
public class SpringSecurityTest
        extends RestAssuredBaseTest
{

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/users");
    }

    @Test
    public void testThatAnonymousUserHasUserViewAccessAccordingToAuthorities()
    {
        final String username = "admin";

        String url = getContextBaseUrl() + "/{username}";
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url, username)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(username));
    }

    @Test
    public void testUnauthorizedRequest()
    {
        // clear default anonymous authorization context and disable it's population
        SecurityContextHolder.getContext()
                             .setAuthentication(null);

        String url = getContextBaseUrl();
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
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

        String url = getContextBaseUrl();
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(user)
               .when()
               .put(url)
               .peek()
               .then()
               .body("error", equalTo("forbidden"))
               .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
