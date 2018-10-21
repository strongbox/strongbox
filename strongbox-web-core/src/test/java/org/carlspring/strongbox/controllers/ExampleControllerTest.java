package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.ExampleForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.*;

/**
 * @author Przemyslaw Fusik
 * @author Steve Todorov
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class ExampleControllerTest
        extends RestAssuredBaseTest
{

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/example-controller");
    }

    @Test
    public void testGetExamplesResponse()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/all")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("examples", hasSize(greaterThan(0)));
    }

    @Test
    public void testGetExampleResponse()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/get/foo-bar")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("name", not(nullValue()));
    }

    @Test
    public void testGetNonExistingJsonExampleResponse()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/get/not-found")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body("message", not(nullValue()));
    }

    @Test
    public void testGetNonExistingPlainExampleResponse()
            throws Exception
    {
        given().accept(MediaType.TEXT_PLAIN_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/get/not-found")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(containsString(ExampleController.NOT_FOUND_MESSAGE));
    }

    @Test
    public void testDeleteExampleResponse()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(getContextBaseUrl() + "/delete/foo-bar")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", not(nullValue()));
    }

    @Test
    public void testDeleteNonExistingExampleResponse()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(getContextBaseUrl() + "/delete/not-found")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body("message", not(nullValue()));
    }

    @Test
    public void testBadFormRequestWithJsonResponse()
            throws Exception
    {
        ExampleForm exampleForm = new ExampleForm();
        exampleForm.setPassword("god");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(exampleForm)
               .when()
               .post(getContextBaseUrl() + "/update/foo-bar")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("message", not(nullValue()))
               .body("errors", hasSize(greaterThan(0)));
    }

    @Test
    public void testBadFormRequestWithPlainTextResponse()
            throws Exception
    {
        ExampleForm exampleForm = new ExampleForm();
        exampleForm.setPassword("god");

        given().accept(MediaType.TEXT_PLAIN_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(exampleForm)
               .when()
               .post(getContextBaseUrl() + "/update/foo-bar")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString("Validation error"));
    }

    @Test
    public void testEmptyFormRequestBodyWithJsonResponse()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .post(getContextBaseUrl() + "/update/foo-bar")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body("message", not(nullValue()));
    }

    @Test
    public void testEmptyFormRequestBodyWithPlainTextResponse()
            throws Exception
    {
        given().accept(MediaType.TEXT_PLAIN_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .post(getContextBaseUrl() + "/update/foo-bar")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString("Empty request body"));
    }

    @Test
    public void testValidFormRequestWithJsonResponse()
            throws Exception
    {
        ExampleForm exampleForm = new ExampleForm();
        exampleForm.setPassword("abcDEF1234");
        exampleForm.setUsername("my-username");

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(exampleForm)
               .when()
               .post(getContextBaseUrl() + "/update/foo-bar")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", not(nullValue()));
    }

    @Test
    public void testValidFormRequestWithPlainTextResponse()
            throws Exception
    {
        ExampleForm exampleForm = new ExampleForm();
        exampleForm.setPassword("abcDEF1234");
        exampleForm.setUsername("my-username");

        given().accept(MediaType.TEXT_PLAIN_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(exampleForm)
               .when()
               .post(getContextBaseUrl() + "/update/foo-bar")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString("Credentials have been successfully updated"));
    }

    @Test
    public void testExceptionHandlingWithJsonResponse()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/handle-exception")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
               .body("message",
                     containsString("This example message will be logged in the logs and sent to the client."));
    }

    @Test
    public void testExceptionHandlingWithPlainTextResponse()
            throws Exception
    {
        given().accept(MediaType.TEXT_PLAIN_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/handle-exception")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
               .body(containsString("This example message will be logged in the logs and sent to the client."));
    }

    @Test
    public void testUnhandledExceptionHandlingWithJsonResponse()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/unhandled-exception")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
               .body("error", containsString("Something bad happened."));
    }

    @Test
    public void testUnhandledExceptionHandlingWithPlainTextResponse()
            throws Exception
    {
        given().accept(MediaType.TEXT_PLAIN_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/unhandled-exception")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
               .body(containsString("Something bad happened."));
    }


}
