package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

/**
 * @author: adavid9
 */
@IntegrationTest
public class ActuatorEndpointsControllerTest
        extends RestAssuredBaseTest
{

    private final String UNAUTHORIZED_MESSAGE = "Full authentication is required to access this resource";

    private static final String LOGGER_PACKAGE = "org.carlspring.strongbox";

    private static final String METRIC_NAME = "process.start.time";

    private static final String NOT_EXISTING_METRIC_NAME = "process.start.time.test";


    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/monitoring");
    }

    @Test
    public void testMonitoringEndpointWithAuthorizedUser()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(notNullValue());
    }

    @Test
    @WithAnonymousUser
    public void testMonitoringEndpointWithUnauthorizedUser()
    {
        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl())
               .peek()
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo(UNAUTHORIZED_MESSAGE));
    }

    @Test
    public void testHealthEndpointWithAuthorizedUser()
    {
        String url = getContextBaseUrl() + "/health";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(notNullValue());
    }

    @Test
    @WithAnonymousUser
    public void testHealthEndpointWithUnauthorizedUser()
    {
        String url = getContextBaseUrl() + "/health";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo(UNAUTHORIZED_MESSAGE));
    }

    @Test
    public void testHealthComponentEndpointWithAuthorizedUser()
    {
        String url = getContextBaseUrl() + "/health/db";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(notNullValue());
    }

    @Test
    @WithAnonymousUser
    public void testHealthComponentEndpointWithUnauthorizedUser()
    {
        String url = getContextBaseUrl() + "/health/db";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo(UNAUTHORIZED_MESSAGE));
    }

    @Test
    public void testHealthNotExistingComponentEndpoint()
    {
        String url = getContextBaseUrl() + "/health/not_existing_component";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testInfoEndpointWithAuthorizedUser()
    {
        String url = getContextBaseUrl() + "/info";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(notNullValue());
    }

    @Test
    @WithAnonymousUser
    public void testInfoEndpointWithUnauthorizedUser()
    {
        String url = getContextBaseUrl() + "/info";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo(UNAUTHORIZED_MESSAGE));
    }

    @Test
    public void testBeansEndpointWithAuthorizedUser()
    {
        String url = getContextBaseUrl() + "/beans";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(notNullValue());
    }

    @Test
    @WithAnonymousUser
    public void testBeansEndpointWithUnauthorizedUser()
    {
        String url = getContextBaseUrl() + "/beans";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo(UNAUTHORIZED_MESSAGE));
    }

    @Test
    public void testMetricsEndpointWithAuthorizedUser()
    {

        String url = getContextBaseUrl() + "/metrics";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(notNullValue());
    }

    @Test
    @WithAnonymousUser
    public void testMetricsEndpointWithUnauthorizedUser()
    {
        String url = getContextBaseUrl() + "/metrics";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo(UNAUTHORIZED_MESSAGE));
    }

    @Test
    public void testSingleMetricEndpointWithAuthorizedUser()
    {
        String url = getContextBaseUrl() + "/metrics/" + METRIC_NAME;

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(notNullValue());
    }

    @Test
    @WithAnonymousUser
    public void testSingleMetricEndpointWithUnauthorizedUser()
    {
        String url = getContextBaseUrl() + "/metrics/" + METRIC_NAME;

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo(UNAUTHORIZED_MESSAGE));
    }

    @Test
    public void testNotExistingMetricEndpoint()
    {
        String url = getContextBaseUrl() + "/metrics/" + NOT_EXISTING_METRIC_NAME;

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testLoggersEndpointWithAuthorizedUser()
    {
        String url = getContextBaseUrl() + "/loggers";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(notNullValue());
    }

    @Test
    @WithAnonymousUser
    public void testLoggersEndpointWithUnauthorizedUser()
    {
        String url = getContextBaseUrl() + "/loggers";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo(UNAUTHORIZED_MESSAGE));
    }

    @Test
    public void testLoggersSinglePackageEndpointWithAuthorizedUser()
    {
        String url = getContextBaseUrl() + "/loggers/" + LOGGER_PACKAGE;

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(notNullValue());
    }

    @Test
    @WithAnonymousUser
    public void testLoggersSinglePackageEndpointWithUnauthorizedUser()
    {
        String url = getContextBaseUrl() + "/loggers/" + LOGGER_PACKAGE;

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", equalTo(UNAUTHORIZED_MESSAGE));
    }

}
