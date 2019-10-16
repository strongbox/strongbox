package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;

import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author: adavid9
 */
@IntegrationTest
public class ActuatorEndpointControllerTest
        extends RestAssuredBaseTest
{

    private static final String LOGGER_PACKAGE = "org.carlspring.strongbox";

    private static final String METRIC_NAME = "process.start.time";

    private static final String NOT_EXISTING_METRIC_NAME = "process.start.time.test";

    @Inject
    private PropertiesBooter propertiesBooter;

    @Inject
    private MockMvcRequestSpecification mockMvc;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/monitoring");
    }

    @Test
    @WithUserDetails("admin")
    public void testStrongboxInfo()
    {

        String url = getContextBaseUrl() + "/info";

        mockMvc.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("strongbox", notNullValue());

        String version = propertiesBooter.getStrongboxVersion();

        mockMvc.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("strongbox.version", equalTo(version));

        String revision = propertiesBooter.getStrongboxRevision();

        mockMvc.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("strongbox.revision", equalTo(revision));
    }

    @WithAnonymousUser
    @ParameterizedTest
    @ValueSource(strings = { "",
                             "/health",
                             "/health/db",
                             "/info",
                             "/beans",
                             "/metrics",
                             "/metrics/" + METRIC_NAME,
                             "/loggers",
                             "/loggers/" + LOGGER_PACKAGE })
    public void testEndpointsWithUnauthorizedUser(final String endpoint)
    {
        String url = getContextBaseUrl() + endpoint;

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.UNAUTHORIZED.value())
               .body("error", CoreMatchers.equalTo(getI18nInsufficientAuthenticationErrorMessage()));
    }

    @ParameterizedTest
    @ValueSource(strings = { "",
                             "/health",
                             "/health/db",
                             "/info",
                             "/beans",
                             "/metrics",
                             "/metrics/" + METRIC_NAME,
                             "/loggers",
                             "/loggers/" + LOGGER_PACKAGE })
    public void testEndpointsWithAuthorizedUser(final String endpoint)
    {
        String url = getContextBaseUrl() + endpoint;

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(CoreMatchers.notNullValue());
    }

    @ParameterizedTest
    @ValueSource(strings = { "/metrics/" + NOT_EXISTING_METRIC_NAME,
                             "/health/not_existing_component" })
    public void testNonExistingEndpoints(final String endpoint)
    {
        String url = getContextBaseUrl() + endpoint;

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value());
    }
}
