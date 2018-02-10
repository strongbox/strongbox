package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.data.PropertyUtils;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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
import static org.junit.Assert.assertTrue;

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

    private static final String LOGS_HOME_DIRECTORY = PropertyUtils.getVaultDirectory();

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

    @Test
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


        String logDirectoryHomeUrl = getContextBaseUrl() + "/logging/logs/";

        //When
        //Getting the table elements
        String tableElementsAsString = given()
                                               .contentType(MediaType.TEXT_PLAIN_VALUE)
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
            assertTrue("The log files should be in the HTML response body!", shouldContainLogFilesInHtmlTableElement);

        }
        finally
        {
            //Delete the temporary log files even if the test fails
            deleteTestLogFilesAndDirectories(false);

        }
    }

    @Test
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

        String logSubDirectoryUrl = getContextBaseUrl() + "/logging/logs/test/";

        //When
        //Getting the table elements
        String tableElementsAsString = given()
                                               .contentType(MediaType.TEXT_PLAIN_VALUE)
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
            assertTrue("The log files should be in the HTML response body!", shouldContainLogFilesInHtmlTableElement);

        }
        finally
        {

            //Delete the test sub directory even if the test fails
            deleteTestLogFilesAndDirectories(true);

        }

    }


    //This method creates temporary log files, and if necessary for subdirectory browsing, a log subdirectory.
    private static Path[] createTestLogFilesAndDirectories(boolean shouldICreateATestSubDirectory)
    {

        //If a test directory is needed, a new directory called `test` under `/logs/` will be created.
        //Otherwise the path of `/logs` will be returned.
        Path logDirectoryPath;
        Path[] paths = new Path[4];
        try
        {

            if (shouldICreateATestSubDirectory)
            {
                logDirectoryPath = Paths.get(LOGS_HOME_DIRECTORY, "/logs/test");
                Files.createDirectory(logDirectoryPath);
            }
            else
            {
                logDirectoryPath = Paths.get(LOGS_HOME_DIRECTORY, "/logs");
            }
            //Create 4 temporary log files from 0 to 3.
            for (int i = 0; i < 4; i++)
            {
                paths[i] = Files.createTempFile(logDirectoryPath, "TestLogFile" + i, ".log");
            }

        }
        catch (IOException e)
        {
            System.out.println("\n\nUNABLE TO CREATE TEST LOG FILES AND/OR DIRECTORIES\n\n");
            e.printStackTrace();
        }
        return paths;
    }

    //This method deletes temporary log files, and if used for subdirectory browsing, the test log subdirectory.
    private static void deleteTestLogFilesAndDirectories(boolean wasATestSubDirectoryCreated)
    {
        //This local class extends the SimpleFileVisitor and overrides the `visitFile` method to delete any
        //Test Log Files upon encountering it.
        class SFVExtend
                extends SimpleFileVisitor<Path>
        {

            @Override
            public FileVisitResult visitFile(Path file,
                                             BasicFileAttributes attrs)
                    throws IOException
            {

                //Possessive Regex for speed
                if (file.getFileName()
                        .toString()
                        .matches("TestLog.*+"))
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
                Path pathToLogHomeDirectory = Paths.get(LOGS_HOME_DIRECTORY, "/logs/test");

                Files.walkFileTree(pathToLogHomeDirectory, new SFVExtend());

                Files.delete(Paths.get(LOGS_HOME_DIRECTORY, "/logs/test"));
            }
            else
            {
                Path pathToLogHomeDirectory = Paths.get(LOGS_HOME_DIRECTORY, "/logs/");

                Files.walkFileTree(pathToLogHomeDirectory, new SFVExtend());
            }
        }
        catch (IOException e)
        {
            System.out.println("\n\nUNABLE TO DELETE TEST LOG FILES AND/OR DIRECTORIES\n\n");
            e.printStackTrace();
        }
    }
}
