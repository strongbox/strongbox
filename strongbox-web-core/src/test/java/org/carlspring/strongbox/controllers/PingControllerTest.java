package org.carlspring.strongbox.controllers;

import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.MavenRestAssuredBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * @author Steve Todorov
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class PingControllerTest
        extends MavenRestAssuredBaseTest
{

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/ping");
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void shouldReturnPong(String acceptHeader)
    {
        ValidatableMockMvcResponse response = given().accept(acceptHeader)
                                                     .when()
                                                     .get(getContextBaseUrl())
                                                     .peek()
                                                     .then()
                                                     .statusCode(HttpStatus.OK.value());

        String message = "pong";
        validateResponseBody(response, acceptHeader, message);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void shouldReturnPongForAuthenticatedUsers(String acceptHeader)
    {
        ValidatableMockMvcResponse response = given().accept(acceptHeader)
                                                     .when()
                                                     .get(getContextBaseUrl() + "/token")
                                                     .peek()
                                                     .then()
                                                     .statusCode(HttpStatus.OK.value());

        String message = "pong";
        validateResponseBody(response, acceptHeader, message);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    @WithAnonymousUser
    void anonymousUsersShouldNotBeAbleToAccess(String acceptHeader)
    {
        given().accept(acceptHeader)
               .when()
               .get(getContextBaseUrl() + "/token")
               .peek()
               .then()
               .log()
               .all()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body(notNullValue());
    }

    private void validateResponseBody(ValidatableMockMvcResponse response,
                                      String acceptHeader,
                                      String message)
    {
        if (acceptHeader.equals(MediaType.APPLICATION_JSON_VALUE))
        {
            response.body("message", equalTo(message));
        }
        else if (acceptHeader.equals(MediaType.TEXT_PLAIN_VALUE))
        {
            response.body(equalTo(message));
        }
        else
        {
            throw new IllegalArgumentException("Unsupported content type: " + acceptHeader);
        }
    }
}
