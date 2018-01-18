package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.forms.ExampleForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;

/**
 * @author Przemyslaw Fusik
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class ValidationExampleControllerTest
        extends RestAssuredBaseTest
{

    @Test
    public void badRequestIsExpected()
            throws Exception
    {
        ExampleForm exampleForm = new ExampleForm();
        exampleForm.setPassword("god");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(exampleForm)
               .when()
               .post("/validation-example/post")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(400);
    }

    @Test
    public void shouldDisallowEmptyRequestBody()
            throws Exception
    {
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .post("/validation-example/post")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(400);
    }

    @Test
    public void validationShouldBeOk()
            throws Exception
    {
        ExampleForm exampleForm = new ExampleForm();
        exampleForm.setPassword("abcDEF1234");
        exampleForm.setUsername("my-username");

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(exampleForm)
               .when()
               .post("/validation-example/post")
               .peek() // Use peek() to print the output
               .then()
               .statusCode(200);
    }

}
