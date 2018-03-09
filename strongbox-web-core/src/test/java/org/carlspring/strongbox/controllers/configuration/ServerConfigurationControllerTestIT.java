package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.controllers.support.BaseUrlEntityBody;
import org.carlspring.strongbox.controllers.support.PortEntityBody;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class ServerConfigurationControllerTestIT
        extends RestAssuredBaseTest
{

    @Test
    public void testSetAndGetPort()
    {
        int newPort = 18080;

        String url = getContextBaseUrl() + "/api/configuration/strongbox/port/" + newPort;

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The port was updated."));

        url = getContextBaseUrl() + "/api/configuration/strongbox/port";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("port", equalTo(newPort));
    }

    @Test
    public void testSetAndGetPortWithBody()
    {
        int newPort = 18080;
        PortEntityBody portEntity = new PortEntityBody(newPort);

        String url = getContextBaseUrl() + "/api/configuration/strongbox/port";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(portEntity)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The port was updated."));

        url = getContextBaseUrl() + "/api/configuration/strongbox/port";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("port", equalTo(newPort));
    }

    @Test
    public void testSetAndGetBaseUrl()
    {
        String newBaseUrl = "http://localhost:" + 40080 + "/newurl";
        BaseUrlEntityBody baseUrlEntity = new BaseUrlEntityBody(newBaseUrl);

        String url = getContextBaseUrl() + "/api/configuration/strongbox/baseUrl";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(baseUrlEntity)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The base URL was updated."));

        url = getContextBaseUrl() + "/api/configuration/strongbox/baseUrl";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("baseUrl", equalTo(newBaseUrl));
    }
}
