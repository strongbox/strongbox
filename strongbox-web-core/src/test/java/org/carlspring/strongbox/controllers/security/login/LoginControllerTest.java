package org.carlspring.strongbox.controllers.security.login;

import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.*;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class LoginControllerTest
        extends RestAssuredBaseTest
{

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
                          .post("/login")
                          .peek()
                          .then()
                          .body("token", CoreMatchers.any(String.class))
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
                          .post("/login")
                          .peek()
                          .then()
                          .body("error", CoreMatchers.equalTo("invalid.credentials"))
                          .statusCode(401);
    }

}