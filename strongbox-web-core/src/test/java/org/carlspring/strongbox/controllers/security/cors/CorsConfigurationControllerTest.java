package org.carlspring.strongbox.controllers.security.cors;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.*;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class CorsConfigurationControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    private CorsConfigurationSource corsConfigurationSource;

    private Map<String, CorsConfiguration> initialConfiguration;

    private static final String url = "/configuration/cors";

    @Before
    public void before()
    {
        initialConfiguration = new HashMap<>(
                ((UrlBasedCorsConfigurationSource) corsConfigurationSource).getCorsConfigurations());
    }

    @After
    public void after()
    {
        ((UrlBasedCorsConfigurationSource) corsConfigurationSource).setCorsConfigurations(initialConfiguration);
    }

    @Test
    public void testUpdateWithEmptyCollectionAndJsonResponse()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(Collections.emptyList())
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", containsString(CorsConfigurationController.SUCCESSFUL_UPDATE));

        // follow-up check to ensure records has been properly saved.
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("origins", hasSize(0));

    }

    @Test
    public void testUpdateWithEmptyCollectionAndTextResponse()
            throws Exception
    {
        given().accept(MediaType.TEXT_PLAIN_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(Collections.emptyList())
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(CorsConfigurationController.SUCCESSFUL_UPDATE));

        // follow-up check to ensure records has been properly saved.
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("origins", hasSize(0));
    }

    @Test
    public void testAllowOneOrigin()
            throws Exception
    {
        given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(Collections.singletonList("http://example.com"))
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", containsString(CorsConfigurationController.SUCCESSFUL_UPDATE));

        // follow-up check to ensure records has been properly saved.
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("origins", hasSize(1));
    }

    @Test
    public void testAllowAllOrigins()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(Collections.singletonList("*"))
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", containsString(CorsConfigurationController.SUCCESSFUL_UPDATE));

        // follow-up check to ensure records has been properly saved.
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("origins", hasSize(1))
               .body("origins", hasItem("*"));
    }

    @Test
    public void testAllowMultipleOrigins()
            throws Exception
    {
        given().header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .body(Arrays.asList("http://example.com", "https://google.com",
                                   "http://dev.carlspring.org/confluence",
                                   "http://dev.carlspring.org/jenkins"))
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", containsString(CorsConfigurationController.SUCCESSFUL_UPDATE));

        // follow-up check to ensure records has been properly saved.
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("origins", hasSize(equalTo(4)))
               .body("origins", hasItem("https://google.com"));
    }
}
