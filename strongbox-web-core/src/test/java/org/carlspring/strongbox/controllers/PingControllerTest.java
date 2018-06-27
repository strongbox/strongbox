package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;

import org.apache.http.HttpHeaders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author Steve Todorov
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class PingControllerTest
        extends MavenRestAssuredBaseTest
{

    @Test
    public void testShouldReturnPongText()
    {
        String url = getContextBaseUrl() + "/api/ping";

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo("pong"));
    }

    @Test
    public void testShouldReturnPongJSON()
    {
        String url = getContextBaseUrl() + "/api/ping";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("pong"));
    }

}
