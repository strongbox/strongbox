package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.rest.context.RestletTestContext;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static io.restassured.RestAssured.get;

/**
 * @author Alex Oreshkevich
 */
@RunWith(SpringJUnit4ClassRunner.class)
@RestletTestContext
//@WebAppConfiguration
@Ignore
public class RestAssuredExampleTest
{

    @BeforeClass
    public static void setupRA() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.requestSpecification = new RequestSpecBuilder()
                                                   .addHeader("authorization", "abracadabra")
                                                   .build();
    }

    @AfterClass
    public static void teardown() throws Exception {
        RestAssured.reset();
    }

    @Test
    public void simpleTest(){
                get("/users/greet")
                .then()
                .statusCode(HttpStatus.OK.value());
    }
}
