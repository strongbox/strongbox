package org.carlspring.strongbox.controllers.login;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.users.dto.UserDto;
import org.carlspring.strongbox.users.service.UserService;

import javax.inject.Inject;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class LoginControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    private UserService userService;

    @Test
    public void shouldReturnGeneratedToken()
            throws Exception
    {
        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("admin");
        loginInput.setPassword("password");

        RestAssuredMockMvc.given()
                          .contentType("application/json")
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
            throws Exception
    {
        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("przemyslaw_fusik");
        loginInput.setPassword("password");

        RestAssuredMockMvc.given()
                          .contentType("application/json")
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
        throws Exception
    {
        UserDto disabledUser = new UserDto();
        disabledUser.setUsername("test-disabled-user-login");
        disabledUser.setPassword("1234");
        disabledUser.setEnabled(false);
        userService.add(disabledUser);

        LoginInput loginInput = new LoginInput();
        loginInput.setUsername("test-disabled-user-login");
        loginInput.setPassword("1234");

        RestAssuredMockMvc.given()
                          .contentType("application/json")
                          .header("Accept", "application/json")
                          .body(loginInput)
                          .when()
                          .post("/api/login")
                          .peek()
                          .then()
                          .body("error", CoreMatchers.equalTo("User account is locked"))
                          .statusCode(401);
    }

}
