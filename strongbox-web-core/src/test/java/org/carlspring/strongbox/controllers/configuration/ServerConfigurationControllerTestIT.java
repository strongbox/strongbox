package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.controllers.support.BaseUrlEntityBody;
import org.carlspring.strongbox.controllers.support.InstanceNameEntityBody;
import org.carlspring.strongbox.controllers.support.PortEntityBody;
import org.carlspring.strongbox.forms.configuration.ServerSettingsForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.controllers.configuration.ServerConfigurationController.FAILED_SAVE_SERVER_SETTINGS;
import static org.carlspring.strongbox.controllers.configuration.ServerConfigurationController.SUCCESSFUL_SAVE_SERVER_SETTINGS;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class ServerConfigurationControllerTestIT
        extends RestAssuredBaseTest
{

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        setContextBaseUrl(getContextBaseUrl() + "/api/configuration/strongbox");
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_SET_PORT",
                                  "CONFIGURATION_VIEW_PORT" })
    public void testSetAndGetPort()
    {
        int newPort = 18080;

        String url = getContextBaseUrl() + "/port/" + newPort;

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The port was updated."));

        url = getContextBaseUrl() + "/port";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("port", equalTo(newPort));
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_SET_PORT",
                                  "CONFIGURATION_VIEW_PORT" })
    public void testSetAndGetPortWithBody()
    {
        int newPort = 18080;
        PortEntityBody portEntity = new PortEntityBody(newPort);

        String url = getContextBaseUrl() + "/port";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(portEntity)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The port was updated."));

        url = getContextBaseUrl() + "/port";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("port", equalTo(newPort));
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_SET_INSTANCE_NAME",
                                  "CONFIGURATION_VIEW_INSTANCE_NAME" })
    public void testSetAndGetInstanceName()
    {
        String instanceName = "strongbox_" + System.currentTimeMillis();
        InstanceNameEntityBody instanceNameEntity = new InstanceNameEntityBody(instanceName);

        String url = getContextBaseUrl() + "/instanceName";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(instanceNameEntity)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The instance's name was updated."));

        url = getContextBaseUrl() + "/instanceName";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("instanceName", equalTo(instanceName));
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_SET_BASE_URL",
                                  "CONFIGURATION_VIEW_BASE_URL" })
    public void testSetAndGetBaseUrl()
    {
        String newBaseUrl = "http://localhost:" + 40080 + "/newurl";
        BaseUrlEntityBody baseUrlEntity = new BaseUrlEntityBody(newBaseUrl);

        String url = getContextBaseUrl() + "/baseUrl";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(baseUrlEntity)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The base URL was updated."));

        url = getContextBaseUrl() + "/baseUrl";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("baseUrl", equalTo(newBaseUrl));
    }

    public void serverSettingsShouldBeSaved(String acceptHeader,
                                            String baseUrl,
                                            int port)
    {
        // assign settings to server
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm(baseUrl, port);

        String url = getContextBaseUrl() + "/serverSettings";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(serverSettingsForm)
               .when()
               .post(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_SAVE_SERVER_SETTINGS));
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_SET_BASE_URL",
                                  "CONFIGURATION_SET_PORT" })
    public void testServerSettingsShouldBeSavedWithResponseInJson()
    {
        String newBaseUrl = "http://localhost:" + 40080 + "/newurl";
        int newPort = 18080;
        serverSettingsShouldBeSaved(MediaType.APPLICATION_JSON_VALUE, newBaseUrl, newPort);
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_SET_BASE_URL",
                                  "CONFIGURATION_SET_PORT" })
    public void testServerSettingsShouldBeSavedWithResponseInText()
    {
        String newBaseUrl = "http://localhost:" + 40080 + "/newurl";
        int newPort = 18080;
        serverSettingsShouldBeSaved(MediaType.TEXT_PLAIN_VALUE, newBaseUrl, newPort);
    }

    public void serverSettingsShouldNotBeSaved(String acceptHeader,
                                               String baseUrl,
                                               int port)
    {
        // assign settings to server
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm(baseUrl, port);

        String url = getContextBaseUrl() + "/serverSettings";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(serverSettingsForm)
               .when()
               .post(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value()) // check http status code
               .body(containsString(FAILED_SAVE_SERVER_SETTINGS));
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_SET_BASE_URL",
                                  "CONFIGURATION_SET_PORT" })
    public void testWrongServerSettingsShouldNotBeSavedWithResponseInJson()
    {
        String newBaseUrl = "";
        int newPort = 0;
        serverSettingsShouldNotBeSaved(MediaType.APPLICATION_JSON_VALUE, newBaseUrl, newPort);
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_SET_BASE_URL",
                                  "CONFIGURATION_SET_PORT" })
    public void testWrongServerSettingsShouldNotBeSavedWithResponseInText()
    {
        String newBaseUrl = null;
        int newPort = 65536;
        serverSettingsShouldNotBeSaved(MediaType.TEXT_PLAIN_VALUE, newBaseUrl, newPort);
    }
}
