package org.carlspring.strongbox.controllers.login;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

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

}
