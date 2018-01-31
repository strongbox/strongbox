package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.get;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class LoggingManagementControllerTestIT
        extends RestAssuredBaseTest
{

    private static final String LOGGER_PACKAGE = "org.carlspring.strongbox";

    private static final String LOGGER_PACKAGE_NON_EXISTING = "org.carlspring.strongbox.test";

    private static final String LOGGER_LEVEL = Level.INFO.getName();

    private static final String LOGGER_APPENDER = "CONSOLE";

    @Test
    public void testAddLoggerWithTextAcceptHeader()
            throws Exception
    {

        String url = getContextBaseUrl() + "/logging/logger";

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .param("logger", LOGGER_PACKAGE)
               .param("level", LOGGER_LEVEL)
               .param("appenderName", LOGGER_APPENDER)
               .when()
               .put(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(equalTo("The logger was added successfully."));
    }

    @Test
    public void testAddLoggerWithJsonAcceptHeader()
            throws Exception
    {

        String url = getContextBaseUrl() + "/logging/logger";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("logger", LOGGER_PACKAGE)
               .param("level", LOGGER_LEVEL)
               .param("appenderName", LOGGER_APPENDER)
               .when()
               .put(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The logger was added successfully."));
    }

    @Test
    public void testUpdateLoggerWithTextAcceptHeader()
            throws Exception
    {

        String url = getContextBaseUrl() + "/logging/logger";

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .param("logger", LOGGER_PACKAGE)
               .param("level", LOGGER_LEVEL)
               .when()
               .post(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(equalTo("The logger was updated successfully."));
    }

    @Test
    public void testUpdateLoggerWithJsonAcceptHeader()
            throws Exception
    {

        String url = getContextBaseUrl() + "/logging/logger";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("logger", LOGGER_PACKAGE)
               .param("level", LOGGER_LEVEL)
               .when()
               .post(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The logger was updated successfully."));
    }

    @Test
    public void testUpdateLoggerNotFoundWithTextAcceptHeader()
            throws Exception
    {

        String url = getContextBaseUrl() + "/logging/logger";
        String loggerPackage = LOGGER_PACKAGE_NON_EXISTING;

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .param("logger", loggerPackage)
               .param("level", LOGGER_LEVEL)
               .when()
               .post(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value()) // check http status code
               .body(equalTo("Logger '" + loggerPackage + "' not found!"));
    }

    @Test
    public void testUpdateLoggerNotFoundWithJsonAcceptHeader()
            throws Exception
    {

        String url = getContextBaseUrl() + "/logging/logger";
        String loggerPackage = LOGGER_PACKAGE_NON_EXISTING;

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("logger", loggerPackage)
               .param("level", LOGGER_LEVEL)
               .when()
               .post(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value()) // check http status code
               .body("message", equalTo("Logger '" + loggerPackage + "' not found!"));
    }

    @Test
    public void testDeleteLoggerWithTextAcceptHeader()
            throws Exception
    {

        String url = getContextBaseUrl() + "/logging/logger";

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .param("logger", LOGGER_PACKAGE)
               .when()
               .delete(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(equalTo("The logger was deleted successfully."));
    }

    @Test
    public void testDeleteLoggerWithJsonAcceptHeader()
            throws Exception
    {

        String url = getContextBaseUrl() + "/logging/logger";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("logger", LOGGER_PACKAGE)
               .when()
               .delete(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("The logger was deleted successfully."));
    }

    @Test
    public void testDeleteLoggerNotFoundWithTextAcceptHeader()
            throws Exception
    {

        String url = getContextBaseUrl() + "/logging/logger";
        String loggerPackage = LOGGER_PACKAGE_NON_EXISTING;

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .param("logger", loggerPackage)
               .when()
               .delete(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value()) // check http status code
               .body(equalTo("Logger '" + loggerPackage + "' not found!"));
    }

    @Test
    public void testDeleteLoggerNotFoundWithJsonAcceptHeader()
            throws Exception
    {

        String url = getContextBaseUrl() + "/logging/logger";
        String loggerPackage = LOGGER_PACKAGE_NON_EXISTING;

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .param("logger", loggerPackage)
               .when()
               .delete(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value()) // check http status code
               .body("message", equalTo("Logger '" + loggerPackage + "' not found!"));
    }

    @Test
    public void testDownloadLog()
            throws Exception
    {
        String logDir = System.getProperty("logging.dir");
        Files.createDirectories(Paths.get(logDir))
             .toFile()
             .deleteOnExit();

        String logName = "strongbox-test.log";
        Files.createFile(Paths.get(logDir, logName))
             .toFile()
             .deleteOnExit();

        String url = getContextBaseUrl() + "/logging/log/" + logName;

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()); // check http status code
    }

    @Test
    public void testDownloadLogbackConfiguration()
            throws Exception
    {
        String url = getContextBaseUrl() + "/logging/logback";

        given().contentType(MediaType.APPLICATION_XML_VALUE)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()); // check http status code
    }

    @Test
    public void testUploadLogbackConfigurationWithTextAcceptHeader()
            throws Exception
    {
        String url = getContextBaseUrl() + "/logging/logback";

        // Obtain the current logback XML.
        byte[] byteArray = get(url).asByteArray();

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .contentType(MediaType.APPLICATION_XML_VALUE)
               .body(byteArray)
               .when()
               .post(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(equalTo("Logback configuration uploaded successfully."));
    }

    @Test
    public void testUploadLogbackConfigurationWithJsonAcceptHeader()
            throws Exception
    {
        String url = getContextBaseUrl() + "/logging/logback";

        // Obtain the current logback XML.
        byte[] byteArray = get(url).asByteArray();

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .contentType(MediaType.APPLICATION_XML_VALUE)
               .body(byteArray)
               .when()
               .post(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("message", equalTo("Logback configuration uploaded successfully."));
    }
}
