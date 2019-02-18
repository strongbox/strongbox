package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author Pablo Tirado
 * @author Aditya Srinivasan
 */
@Disabled // TODO migrate to https://docs.spring.io/spring-boot/docs/current/actuator-api/html/#loggers after https://github.com/strongbox/strongbox/issues/1000
@IntegrationTest
public class LoggingManagementControllerTestIT
        extends RestAssuredBaseTest
{

    private static final String LOGGER_PACKAGE = "org.carlspring.strongbox";

    private static final String LOGGER_PACKAGE_NON_EXISTING = "org.carlspring.strongbox.test";

    private static final String LOGGER_LEVEL = Level.INFO.getName();

    private static final String LOGGER_APPENDER = "CONSOLE";

    @Inject
    private PropertiesBooter propertiesBooter;


    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/logging");
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_ADD_LOGGER" })
    public void testAddLoggerWithTextAcceptHeader()
    {
        String url = getContextBaseUrl() + "/logger";

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
    @WithMockUser(authorities = { "CONFIGURATION_ADD_LOGGER" })
    public void testAddLoggerWithJsonAcceptHeader()
    {
        String url = getContextBaseUrl() + "/logger";

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
    @WithMockUser(authorities = { "CONFIGURATION_UPDATE_LOGGER" })
    public void testUpdateLoggerWithTextAcceptHeader()
    {
        String url = getContextBaseUrl() + "/logger";

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
    @WithMockUser(authorities = { "CONFIGURATION_UPDATE_LOGGER" })
    public void testUpdateLoggerWithJsonAcceptHeader()
    {
        String url = getContextBaseUrl() + "/logger";

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
    @WithMockUser(authorities = { "CONFIGURATION_UPDATE_LOGGER" })
    public void testUpdateLoggerNotFoundWithTextAcceptHeader()
    {
        String url = getContextBaseUrl() + "/logger";
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
    @WithMockUser(authorities = { "CONFIGURATION_UPDATE_LOGGER" })
    public void testUpdateLoggerNotFoundWithJsonAcceptHeader()
    {
        String url = getContextBaseUrl() + "/logger";
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
    @WithMockUser(authorities = { "CONFIGURATION_DELETE_LOGGER" })
    public void testDeleteLoggerWithTextAcceptHeader()
    {
        String url = getContextBaseUrl() + "/logger";

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
    @WithMockUser(authorities = { "CONFIGURATION_DELETE_LOGGER" })
    public void testDeleteLoggerWithJsonAcceptHeader()
    {
        String url = getContextBaseUrl() + "/logger";

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
    @WithMockUser(authorities = { "CONFIGURATION_DELETE_LOGGER" })
    public void testDeleteLoggerNotFoundWithTextAcceptHeader()
    {

        String url = getContextBaseUrl() + "/logger";
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
    @WithMockUser(authorities = { "CONFIGURATION_DELETE_LOGGER" })
    public void testDeleteLoggerNotFoundWithJsonAcceptHeader()
    {
        String url = getContextBaseUrl() + "/logger";
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
    @WithMockUser(authorities = { "CONFIGURE_LOGS" })
    public void testDownloadLog()
            throws Exception
    {
        String testLogName = "strongbox-test.log";
        Files.createFile(Paths.get(propertiesBooter.getLogsDirectory(), testLogName))
             .toFile()
             .deleteOnExit();

        String url = getContextBaseUrl() + "/log/" + testLogName;

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()); // check http status code
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_RETRIEVE_LOGBACK_CFG" })
    public void testDownloadLogbackConfiguration()
    {
        String url = getContextBaseUrl() + "/logback";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value()); // check http status code
    }

    @Test
    @WithMockUser(authorities = { "CONFIGURATION_UPLOAD_LOGBACK_CFG", "CONFIGURATION_RETRIEVE_LOGBACK_CFG" })
    public void testUploadLogbackConfigurationWithTextAcceptHeader()
    {
        String url = getContextBaseUrl() + "/logback";

        // Obtain the current logback XML.
        byte[] byteArray = given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                                  .get(url)
                                  .peek()
                                  .then()
                                  .statusCode(HttpStatus.OK.value())
                                  .extract()
                                  .asByteArray();


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
    @WithMockUser(authorities = { "CONFIGURATION_UPLOAD_LOGBACK_CFG", "CONFIGURATION_RETRIEVE_LOGBACK_CFG"})
    public void testUploadLogbackConfigurationWithJsonAcceptHeader()
    {
        String url = getContextBaseUrl() + "/logback";

        // Obtain the current logback XML.
        byte[] byteArray = given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                                  .get(url)
                                  .peek()
                                  .then()
                                  .statusCode(HttpStatus.OK.value())
                                  .extract()
                                  .asByteArray();

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
    
    @Test
    @WithMockUser(authorities = { "VIEW_LOGS" })
    public void testLogDirectoryForListOfLogFiles()
    {
        //Given
        //Create dummy test files here
        Path[] paths = createTestLogFilesAndDirectories(false);

        String[] tempLogFilesArray = new String[4];
        for (int i = 0; i < paths.length; i++)
        {
            tempLogFilesArray[i] = paths[i].getFileName()
                                           .toString();
        }

        String logDirectoryHomeUrl = getContextBaseUrl() + "/logs/";
        
        //When
        //Getting the table elements
        String tableElementsAsString = given().contentType(MediaType.TEXT_PLAIN_VALUE)
                                              .when()
                                              .get(logDirectoryHomeUrl)
                                              .body()
                                              .htmlPath()
                                              .getString("html.body.table");
        
        //Assertion Test to see if given file names are contained in the HTML body
        boolean shouldContainLogFilesInHtmlTableElement = false;
        if (tableElementsAsString.contains(tempLogFilesArray[0])
            && tableElementsAsString.contains(tempLogFilesArray[1])
            && tableElementsAsString.contains(tempLogFilesArray[2])
            && tableElementsAsString.contains(tempLogFilesArray[3]))
        {
            shouldContainLogFilesInHtmlTableElement = true;
        }
        
        try
        {
            assertTrue(shouldContainLogFilesInHtmlTableElement, "The log files should be in the HTML response body!");
        }
        finally
        {
            //Delete the temporary log files even if the test fails
            deleteTestLogFilesAndDirectories(false);
        }
    }
    
    @Test
    @WithMockUser(authorities = { "VIEW_LOGS" })
    public void testAbilityToNavigateToSubLogDirectories()
    {
        //Given
        //Creating the test sub directory and dummy files here
        Path[] paths = createTestLogFilesAndDirectories(true);
        
        String[] tempLogFilesArray = new String[4];
        for (int i = 0; i < paths.length; i++)
        {
            tempLogFilesArray[i] = paths[i].getFileName()
                                           .toString();
        }
        
        String logSubDirectoryUrl = getContextBaseUrl() + "/logs/test/";
        
        //When
        //Getting the table elements
        String tableElementsAsString = given().contentType(MediaType.TEXT_PLAIN_VALUE)
                                              .when()
                                              .get(logSubDirectoryUrl)
                                              .body()
                                              .htmlPath()
                                              .getString("html.body");
        
        //Assertion Test to see if given file names and test folder are contained in the HTML body
        boolean shouldContainLogFilesInHtmlTableElement = false;
        if (tableElementsAsString.contains(tempLogFilesArray[0])
            && tableElementsAsString.contains(tempLogFilesArray[1])
            && tableElementsAsString.contains(tempLogFilesArray[2])
            && tableElementsAsString.contains(tempLogFilesArray[3])
            && tableElementsAsString.contains("test"))
        {
            shouldContainLogFilesInHtmlTableElement = true;
        }
        
        try
        {
            //Assertion Test
            assertTrue(shouldContainLogFilesInHtmlTableElement, "The log files should be in the HTML response body!");
        }
        finally
        {
            //Delete the test sub directory even if the test fails
            deleteTestLogFilesAndDirectories(true);
        }
    }
    
    //This method creates temporary log files, and if necessary for subdirectory browsing, a log subdirectory.
    private Path[] createTestLogFilesAndDirectories(boolean shouldICreateATestSubDirectory)
    {
        //If a test directory is needed, a new directory called `test` under `/logs/` will be created.
        //Otherwise the path of `/logs` will be returned.
        Path logDirectoryPath;
        Path[] paths = new Path[4];
        try
        {
            
            if (shouldICreateATestSubDirectory)
            {
                logDirectoryPath = Paths.get(propertiesBooter.getLogsDirectory(), "/test");
                Files.createDirectory(logDirectoryPath);
            }
            else
            {
                logDirectoryPath = Paths.get(propertiesBooter.getLogsDirectory());
            }
            
            //Create 4 temporary log files from 0 to 3.
            for (int i = 0; i < 4; i++)
            {
                paths[i] = Files.createTempFile(logDirectoryPath, "TestLogFile" + i, ".log");
            }   
        }
        catch (IOException e)
        {
            fail("Unable to create test log files and/or directories!");
        }
        
        return paths;
    }
    
    //This method deletes temporary log files, and if used for subdirectory browsing, the test log subdirectory.
    private void deleteTestLogFilesAndDirectories(boolean wasATestSubDirectoryCreated)
    {
        
        //This local class extends the SimpleFileVisitor and overrides the `visitFile` method to delete any
        //Test Log Files upon encountering it.
        class LogFileVisitor
                extends SimpleFileVisitor<Path>
        {

            @Override
            public FileVisitResult visitFile(Path file,
                                             BasicFileAttributes attrs)
                    throws IOException
            {
                //Possessive Regex for speed
                if (file.getFileName().toString().matches("TestLog.*+"))
                {
                    Files.delete(file);
                }

                return FileVisitResult.CONTINUE;
            }
        }

        try
        {
            if (wasATestSubDirectoryCreated)
            {
                Path pathToLogHomeDirectory = Paths.get(propertiesBooter.getLogsDirectory(), "/test");

                Files.walkFileTree(pathToLogHomeDirectory, new LogFileVisitor());

                Files.delete(Paths.get(propertiesBooter.getLogsDirectory(), "/test"));
            }
            else
            {
                Path pathToLogHomeDirectory = Paths.get(propertiesBooter.getLogsDirectory());

                Files.walkFileTree(pathToLogHomeDirectory, new LogFileVisitor());
            }
        }
        catch (IOException e)
        {
            fail("Unable to delete test log files and/or directories!");
        }
    }

}
