package org.carlspring.strongbox.controllers.login;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import javax.inject.Inject;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.users.UserForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.service.UserService;
import org.carlspring.strongbox.users.service.impl.EncodedPasswordUser;
import org.carlspring.strongbox.users.service.impl.OrientDbUserService.OrientDb;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;

import com.google.common.collect.ImmutableSet;

import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
public class LoginControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    @OrientDb
    private UserService userService;

    @Inject
    private PasswordEncoder passwordEncoder;
    
    @Inject
    private MockMvcRequestSpecification mockMvc;
    
    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    @AfterEach
    public void afterEach()
    {
    }

    @Test
    public void shouldReturnGeneratedToken()
    {
        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("admin");
        loginInput.setPassword("password");

        mockMvc.contentType("application/json")
               .header("Accept", "application/json")
               .body(loginInput)
               .when()
               .post("/api/login")
               .peek()
               .then()
               .body("token", CoreMatchers.any(String.class))
               .body("authorities", hasSize(greaterThan(0)))
               .statusCode(200);
    }

    @WithAnonymousUser
    @Test
    public void shouldReturnInvalidCredentialsError()
    {
        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("przemyslaw_fusik");
        loginInput.setPassword("password");

        mockMvc.contentType("application/json")
               .header("Accept", "application/json")
               .body(loginInput)
               .when()
               .post("/api/login")
               .peek()
               .then()
               .body("error", CoreMatchers.equalTo("invalid.credentials"))
               .statusCode(401);
    }

    @Test
    @WithAnonymousUser
    public void shouldReturnInvalidCredentialsWhenUserIsDisabled()
    {
        UserDto disabledUser = new UserDto();
        disabledUser.setUsername("test-disabled-user-login");
        disabledUser.setPassword("1234");
        disabledUser.setEnabled(false);
        userService.save(new EncodedPasswordUser(disabledUser, passwordEncoder));

        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("test-disabled-user-login");
        loginInput.setPassword("1234");

        mockMvc.contentType("application/json")
               .header("Accept", "application/json")
               .body(loginInput)
               .when()
               .post("/api/login")
               .peek()
               .then()
               .body("error", CoreMatchers.equalTo("User account is locked"))
               .statusCode(401);
    }

    @Test
    @WithAnonymousUser
    public void userCacheShouldBeClearedAfterPasswordChange()
    {
        UserDto cacheEvictionTestUser = new UserDto();
        cacheEvictionTestUser.setUsername("admin-cache-eviction-test");
        cacheEvictionTestUser.setPassword("password");
        cacheEvictionTestUser.setRoles(ImmutableSet.of("ADMIN"));
        cacheEvictionTestUser.setEnabled(true);
        cacheEvictionTestUser.setSecurityTokenKey("admin-cache-eviction-test-secret");
        userService.save(new EncodedPasswordUser(cacheEvictionTestUser, passwordEncoder));

        
        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("admin-cache-eviction-test");
        loginInput.setPassword("password");

        mockMvc.contentType("application/json")
               .header("Accept", "application/json")
               .body(loginInput)
               .when()
               .post("/api/login")
               .peek()
               .then()
               .statusCode(200)
               .body("token", CoreMatchers.any(String.class))
               .body("authorities", hasSize(greaterThan(0)));

        UserForm userForm = new UserForm();
        userForm.setUsername("admin-cache-eviction-test");
        userForm.setPassword("passwordChanged");

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(userForm)
               .when()
               .put("/api/account")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());

        mockMvc.contentType("application/json")
               .header("Accept", "application/json")
               .body(loginInput)
               .when()
               .post("/api/login")
               .peek()
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", CoreMatchers.equalTo("invalid.credentials"));
    }

}
