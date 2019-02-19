package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.domain.User;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.XmlUserService.XmlUserServiceQualifier;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.transaction.BeforeTransaction;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Steve Todorov
 * @author Pablo Tirado
 */
@IntegrationTest
@ActiveProfiles(profiles = "test")
public class AccountControllerTest
        extends RestAssuredBaseTest
{

    private static final String TEST_DISABLED_USER_ACCOUNT = "test-disabled-user-account";

    @Inject
    @XmlUserServiceQualifier
    private UserService userService;
    
    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/account");
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
    @WithUserDetails("admin")
    public void testGetAccountDetails()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("username", equalTo("admin"));
    }

    @Test
    @WithUserDetails(TEST_DISABLED_USER_ACCOUNT)
    @Transactional
    public void testGetAccountDetailsOnDisabledUserShouldFail()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.FORBIDDEN.value())
               .body("error", notNullValue())
        ;
    }

    @Test
    @WithMockUser(username = "test-account-update", authorities = {"AUTHENTICATED_USER"})
    public void testUpdateAccountDetails()
            throws Exception
    {
        UserDto testUser = new UserDto();
        testUser.setUsername("test-account-update");
        testUser.setPassword("password");
        userService.save(testUser);

        User userEntity = userService.findByUserName(testUser.getUsername());

        // Change security Token
        UserForm userForm = new UserForm();
        userForm.setSecurityTokenKey("1234");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(userForm)
               .when()
               .put(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value());

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("securityTokenKey", equalTo("1234"));


        // Change password & security token
        userForm.setSecurityTokenKey("12345");
        userForm.setPassword("abcde");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(userForm)
               .when()
               .put(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value());

        User updatedUser = userService.findByUserName("admin");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("securityTokenKey", equalTo("12345"));

        assertNotEquals(userEntity.getPassword(), updatedUser.getPassword());
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

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value());

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(userForm)
               .when()
               .put(getContextBaseUrl())
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value());

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
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
    public void testChangingPasswordToNullShouldNotUpdate()
    {
        final String username = "admin";
        UserForm userForm = new UserForm();
        userForm.setPassword(null);

        User originalUser = userService.findByUserName(username);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(userForm)
               .when()
               .put(getContextBaseUrl())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        User updatedUser = userService.findByUserName(username);
        assertEquals(username, updatedUser.getUsername());
        assertNotNull(updatedUser.getPassword());
        assertEquals(originalUser.getPassword(), updatedUser.getPassword());
    }

    @Test
    @WithAnonymousUser
    public void testAnonymousUsersShouldNotBeAbleToAccess()
    {
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek()
               .then()
               .log().all()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body(notNullValue());
    }

}
