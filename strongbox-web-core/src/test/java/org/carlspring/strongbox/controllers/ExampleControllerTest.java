package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.ExampleForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.*;

/**
 * @author Przemyslaw Fusik
 * @author Steve Todorov
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ExampleControllerTest
        extends RestAssuredBaseTest
{

    @Test
    public void testGetExamplesResponse()
            throws Exception
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get("/example-controller/all")
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
               .get("/example-controller/get/foo-bar")
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
               .get("/example-controller/get/not-found")
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
               .get("/example-controller/get/not-found")
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
               .delete("/example-controller/delete/foo-bar")
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
               .delete("/example-controller/delete/not-found")
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
               .post("/example-controller/update/foo-bar")
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
               .post("/example-controller/update/foo-bar")
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
               .post("/example-controller/update/foo-bar")
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
               .post("/example-controller/update/foo-bar")
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
               .post("/example-controller/update/foo-bar")
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
               .post("/example-controller/update/foo-bar")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString("Credentials have been successfully updated"));
    }

}
