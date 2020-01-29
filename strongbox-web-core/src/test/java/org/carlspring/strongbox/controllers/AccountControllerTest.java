package org.carlspring.strongbox.controllers;


import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

import java.util.Arrays;
import java.util.HashSet;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.http.HttpHeaders;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.dto.User;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.OrientDbUserService.OrientDb;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.transaction.BeforeTransaction;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;

/**
 * @author Steve Todorov
 * @author Pablo Tirado
 */
@IntegrationTest
public class AccountControllerTest
        extends RestAssuredBaseTest
{

    private static final String TEST_DISABLED_USER_ACCOUNT = "test-disabled-user-account";

    @Inject
    @OrientDb
    private UserService userService;
    
    @Inject
    private MockMvcRequestSpecification mockMvc;
    
    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/account");
    }

    @BeforeTransaction
    public void setupDisabledUser()
    {
        UserDto disabledUser = new UserDto();
        disabledUser.setUsername(TEST_DISABLED_USER_ACCOUNT);
        disabledUser.setPassword("1234");
        disabledUser.setEnabled(false);
        userService.save(disabledUser);
    }

    @Test
    public void testGetAccountDetails()
    {
        String url = getContextBaseUrl();
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("username", equalTo("admin"));
    }

    @Test
    @WithUserDetails(TEST_DISABLED_USER_ACCOUNT)
    @Transactional
    public void testGetAccountDetailsOnDisabledUserShouldFail()
    {
        String url = getContextBaseUrl();
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value())
               .body("error", notNullValue());
    }

    @Test
    @WithMockUser(username = "test-account-update", authorities = {"AUTHENTICATED_USER"})
    public void testUpdateAccountDetails()
    {
        UserDto testUser = new UserDto();
        testUser.setUsername("test-account-update");
        testUser.setPassword("password");
        userService.save(testUser);

        User userEntity = userService.findByUsername(testUser.getUsername());

        // Change security Token
        UserForm userForm = new UserForm();
        userForm.setSecurityTokenKey("1234");

        String url = getContextBaseUrl();
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(userForm)
               .when()
               .put(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value());

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("securityTokenKey", equalTo("1234"));


        // Change password & security token
        userForm.setSecurityTokenKey("12345");
        userForm.setPassword("abcde");

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(userForm)
               .when()
               .put(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value());

        User updatedUser = userService.findByUsername("test-account-update");

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("securityTokenKey", equalTo("12345"));

        assertThat(updatedUser.getPassword()).isNotEqualTo(userEntity.getPassword());
    }

    @Test
    @WithMockUser(username = "test-account-update-additional", authorities = {"AUTHENTICATED_USER"})
    public void testUpdateAdditionalAccountDetailsShouldNotUpdateThem()
    {
        UserDto testUser = new UserDto();
        testUser.setUsername("test-account-update-additional");
        testUser.setPassword("password");
        testUser.setRoles(null);
        testUser.setEnabled(true);

        userService.save(testUser);

        // Tru to change roles
        UserForm userForm = new UserForm();
        userForm.setRoles(new HashSet<>(Arrays.asList("admin", "super-admin")));
        userForm.setEnabled(false);

        String url = getContextBaseUrl();
        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value());

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(userForm)
               .when()
               .put(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value());

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("roles", hasSize(0))
               .body("enabled", equalTo(true));
    }

    /**
     * The UI could in some cases could pass a "null" password field with the form it's submitting.
     * In those cases the request should pass "as normal", but the password should NOT be changed to null!
     */
    @Test
    @WithMockUser(username = "test-account-update-empty-password", authorities = {"AUTHENTICATED_USER"})
    public void testChangingPasswordToNullShouldNotUpdate()
    {
        final String username = "test-account-update-empty-password";

        UserDto testUser = new UserDto();
        testUser.setUsername(username);
        testUser.setPassword("password");
        testUser.setSourceId(null);
        testUser.setRoles(null);
        testUser.setEnabled(true);
        userService.save(testUser);

        UserForm userForm = new UserForm();
        userForm.setPassword(null);

        User originalUser = userService.findByUsername(username);

        String url = getContextBaseUrl();
        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(userForm)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        User updatedUser = userService.findByUsername(username);
        assertThat(updatedUser.getUsername()).isEqualTo(username);
        assertThat(updatedUser.getPassword()).isNotNull();
        assertThat(updatedUser.getPassword()).isEqualTo(originalUser.getPassword());
    }

    @Test
    @WithAnonymousUser
    public void testAnonymousUsersShouldNotBeAbleToAccess()
    {
        String url = getContextBaseUrl();        
        mockMvc.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .log().all()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body(notNullValue());
    }

}
