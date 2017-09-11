package org.carlspring.strongbox.controllers.security.cors;

import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class CorsConfigurationControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    private CorsConfigurationSource corsConfigurationSource;

    private Map<String, CorsConfiguration> initialConfiguration;

    @Before
    public void before()
    {
        initialConfiguration = new HashMap<>(((UrlBasedCorsConfigurationSource) corsConfigurationSource).getCorsConfigurations());
    }

    @After
    public void after()
    {
        ((UrlBasedCorsConfigurationSource) corsConfigurationSource).setCorsConfigurations(initialConfiguration);
    }

    @Test
    public void shouldReturnExpectedAllowedOrigins()
            throws Exception
    {
        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/cors/")
                          .peek()
                          .then()
                          .statusCode(200)
                          .body(CoreMatchers.equalTo("[ \"*\" ]"));
    }

    @Test
    public void shouldAllowOneOrigin()
            throws Exception
    {
        RestAssuredMockMvc.given()
                          .header("Content-Type", "application/json")
                          .body(Arrays.asList("http://example.com"))
                          .when()
                          .put("/configuration/cors/")
                          .peek()
                          .then()
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/cors/")
                          .peek()
                          .then()
                          .statusCode(200)
                          .body(CoreMatchers.equalTo("[ \"http://example.com\" ]"));
    }

    @Test
    public void shouldAllowMultipleOrigins()
            throws Exception
    {
        RestAssuredMockMvc.given()
                          .header("Content-Type", "application/json")
                          .body(Arrays.asList("http://example.com", "https://google.com",
                                              "http://dev.carlspring.org/confluence",
                                              "http://dev.carlspring.org/jenkins"))
                          .when()
                          .put("/configuration/cors/")
                          .peek()
                          .then()
                          .statusCode(200);

        RestAssuredMockMvc.given()
                          .header("Accept", "application/json")
                          .when()
                          .get("/configuration/cors/")
                          .peek()
                          .then()
                          .statusCode(200)
                          .body(CoreMatchers.equalTo(
                                  "[ \"http://example.com\", \"https://google.com\", \"http://dev.carlspring.org/confluence\", \"http://dev.carlspring.org/jenkins\" ]"));
    }
}