package org.carlspring.strongbox.controllers.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;
import io.restassured.module.mockmvc.internal.MockMvcFactory;
import io.restassured.module.mockmvc.internal.MockMvcRequestSpecificationImpl;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.controllers.support.BaseUrlEntityBody;
import org.carlspring.strongbox.controllers.support.InstanceNameEntityBody;
import org.carlspring.strongbox.controllers.support.MaxUploadSizeEntityBody;
import org.carlspring.strongbox.controllers.support.PortEntityBody;
import org.carlspring.strongbox.forms.configuration.CorsConfigurationForm;
import org.carlspring.strongbox.forms.configuration.ProxyConfigurationForm;
import org.carlspring.strongbox.forms.configuration.ServerSettingsForm;
import org.carlspring.strongbox.forms.configuration.SmtpConfigurationForm;
import org.carlspring.strongbox.rest.client.MockMvcRequestSpecificationProxyTarget;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import org.carlspring.strongbox.testing.artifact.PypiTestArtifact;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.servlet.Filter;

import static org.apache.commons.fileupload.FileUploadBase.CONTENT_LENGTH;
import static org.carlspring.strongbox.controllers.configuration.ServerConfigurationController.FAILED_SAVE_SERVER_SETTINGS;
import static org.carlspring.strongbox.controllers.configuration.ServerConfigurationController.SUCCESSFUL_SAVE_SERVER_SETTINGS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
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

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The port was updated."));

        url = getContextBaseUrl() + "/port";

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
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

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(portEntity)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The port was updated."));

        url = getContextBaseUrl() + "/port";

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
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

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(instanceNameEntity)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The instance's name was updated."));

        url = getContextBaseUrl() + "/instanceName";

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
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

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(baseUrlEntity)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The base URL was updated."));

        url = getContextBaseUrl() + "/baseUrl";

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("baseUrl", equalTo(newBaseUrl));
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_SET_MAX_UPLOAD_SIZE",
            "CONFIGURATION_VIEW_MAX_UPLOAD_SIZE" })
    public void testSetAndGetMaxUploadSize()
    {
        String newSize = "10MB";

        String url = getContextBaseUrl() + "/maxUploadSize/" + newSize;

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .put(url)
                .then()
                .statusCode(HttpStatus.OK.value()) // check http status code
                .body("message", equalTo("The max upload size was updated."));

        url = getContextBaseUrl() + "/maxUploadSize";

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(url)
                .then()
                .statusCode(HttpStatus.OK.value()) // check http status code
                .body("maxUploadSize", equalTo(newSize));
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_SET_MAX_UPLOAD_SIZE",
            "CONFIGURATION_VIEW_MAX_UPLOAD_SIZE" })
    public void testSetAndGetMaxUploadSizeWithBody()
    {
        String newSize = "10MB";
        MaxUploadSizeEntityBody maxUploadEntity = new MaxUploadSizeEntityBody(newSize);

        String url = getContextBaseUrl() + "/maxUploadSize";

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(maxUploadEntity)
                .when()
                .put(url)
                .then()
                .statusCode(HttpStatus.OK.value()) // check http status code
                .body("message", equalTo("The max upload size was updated."));

        mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .get(url)
                .then()
                .statusCode(HttpStatus.OK.value()) // check http status code
                .body("maxUploadSize", equalTo(newSize));
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_SET_MAX_UPLOAD_SIZE",
            "CONFIGURATION_VIEW_MAX_UPLOAD_SIZE", "ARTIFACTS_DEPLOY" })
    public void testMaxUploadSizeExceeded(WebApplicationContext applicationContext) throws Exception
    {
        String smallSize = "7KB";
        String largeSize = "15KB";
        int largeLength = 1024*10;
        int smallLength = 1024*5;

        String setupUrl = getContextBaseUrl() + "/maxUploadSize/"+smallSize;

        int rootEnd = getContextBaseUrl().indexOf("/",9);
        String rootUrl = getContextBaseUrl().substring(0,rootEnd);
        String uploadUrl = rootUrl+"/storages/storage-pypi/pypi-releases";

        // set max upload size to smaller size
        filteredMockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .put(setupUrl)
                .then()
                .statusCode(HttpStatus.OK.value()) // check http status code
                .body("message", equalTo("The max upload size was updated."));

        //  pypi package with too  invalid size upload
        try
        {
            filteredMockMvc.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .header(CONTENT_LENGTH, Integer.toString(largeLength))
                    .multiPart("filetype", "sdist")
                    .multiPart(":action", "file_upload")
                    .multiPart("name", "large-file-size-upload-test")
                    .multiPart("metadata_version", "1.0")
                    .multiPart("content", "large-file-size-upload-test.whl", new byte[largeLength])
                    .when()
                    .post(uploadUrl)
                    .then()
                    .log()
                    .all()
                    .statusCode(HttpStatus.OK.value())
                    .body(Matchers.containsString("The artifact was deployed successfully."));
            fail("Size violation exception not thrown");
        }
        catch (MaxUploadSizeExceededException e)
        {
            // Filter worked correctly
        }

        try
        {
            //  pypi package with accurate size upload
            filteredMockMvc.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .header(CONTENT_LENGTH, Integer.toString(smallLength))
                    .multiPart("filetype", "sdist")
                    .multiPart(":action", "file_upload")
                    .multiPart("name", "small-file-size-upload-test")
                    .multiPart("metadata_version", "1.0")
                    .multiPart("content", "small-file-size-upload-test.whl", new byte[smallLength])
                    .when()
                    .post(uploadUrl)
                    .then()
                    .log()
                    .all()
                    .statusCode(HttpStatus.OK.value())
                    .body(Matchers.containsString("The artifact was deployed successfully."));
        }
        catch (MaxUploadSizeExceededException e)
        {
            fail("Size violation exception was thrown byt the file size is valid");
        }

        setupUrl = getContextBaseUrl() + "/maxUploadSize/"+largeSize;
        // set max upload size to larger size
        filteredMockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                .put(setupUrl)
                .then()
                .statusCode(HttpStatus.OK.value()) // check http status code
                .body("message", equalTo("The max upload size was updated."));

        try
        {
            //  pypi package with accurate size upload
            filteredMockMvc.contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
                    .header(CONTENT_LENGTH, Integer.toString(largeLength))
                    .multiPart("filetype", "sdist")
                    .multiPart(":action", "file_upload")
                    .multiPart("name", "large-file-size-upload-test")
                    .multiPart("metadata_version", "1.0")
                    .multiPart("content", "large-file-size-upload-test.whl", new byte[largeLength])
                    .when()
                    .post(uploadUrl)
                    .then()
                    .log()
                    .all()
                    .statusCode(HttpStatus.OK.value())
                    .body(Matchers.containsString("The artifact was deployed successfully."));
        }
        catch (MaxUploadSizeExceededException e)
        {
            fail("Size violation exception was thrown, but the file size is valid");
        }

    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    @WithMockUser(authorities = { "CONFIGURATION_SET_BASE_URL",
                                  "CONFIGURATION_SET_PORT" })
    void serverSettingsShouldBeSaved(String acceptHeader)
    {

        String baseUrl = "http://localhost:" + 40080 + "/newurl";

        Integer port = 18080;

        CorsConfigurationForm corsConfigurationForm = new CorsConfigurationForm(
                Arrays.asList("http://example.com", "https://github.com/strongbox")
        );

        SmtpConfigurationForm smtpConfigurationForm = new SmtpConfigurationForm(
                "localhost", 25, "tls", "username", "password"
        );

        ProxyConfigurationForm proxyConfigurationForm = new ProxyConfigurationForm(
                "localhost",
                3218,
                "direct",
                "username",
                "password",
                Arrays.asList("http://example.com", "https://github.com/strongbox")
        );

        // assign settings to server
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm(baseUrl,
                                                                       port,
                                                                       "Strongbox-1234",
                                                                       corsConfigurationForm,
                                                                       smtpConfigurationForm,
                                                                       proxyConfigurationForm);

        String url = getContextBaseUrl() + "/serverSettings";

        // Save the form
        mockMvc.log().all().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(serverSettingsForm)
               .when()
               .post(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(containsString(SUCCESSFUL_SAVE_SERVER_SETTINGS));


        // Check if things are properly saved.
        mockMvc.log().all().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("baseUrl", equalTo(baseUrl))
               .body("port", equalTo(port))
               .body("corsConfigurationForm.allowedOrigins", hasSize(equalTo(2)))
               .body("smtpConfigurationForm.host", equalTo(smtpConfigurationForm.getHost()))
               .body("smtpConfigurationForm.port", equalTo(smtpConfigurationForm.getPort()))
               .body("smtpConfigurationForm.connection", equalTo(smtpConfigurationForm.getConnection()))
               .body("smtpConfigurationForm.username", equalTo(smtpConfigurationForm.getUsername()))
               .body("smtpConfigurationForm.password", isEmptyOrNullString())
               .body("proxyConfigurationForm.host", equalTo(proxyConfigurationForm.getHost()))
               .body("proxyConfigurationForm.port", equalTo(proxyConfigurationForm.getPort()))
               .body("proxyConfigurationForm.type", equalTo(proxyConfigurationForm.getType()))
               .body("proxyConfigurationForm.username", equalTo(proxyConfigurationForm.getUsername()))
               .body("proxyConfigurationForm.password", isEmptyOrNullString())
        ;

    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    @WithMockUser(authorities = { "CONFIGURATION_SET_BASE_URL",
                                  "CONFIGURATION_SET_PORT" })
    void serverSettingsShouldNotBeSaved(String acceptHeader)
    {

        String baseUrl = "";

        Integer port = 0;

        // assign settings to server
        ServerSettingsForm serverSettingsForm = new ServerSettingsForm(baseUrl, port);

        String url = getContextBaseUrl() + "/serverSettings";

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(serverSettingsForm)
               .when()
               .post(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value()) // check http status code
               .body(containsString(FAILED_SAVE_SERVER_SETTINGS));
    }
}
