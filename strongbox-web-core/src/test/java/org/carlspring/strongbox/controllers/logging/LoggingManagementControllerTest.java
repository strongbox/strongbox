package org.carlspring.strongbox.controllers.logging;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.domain.DirectoryListing;
import org.carlspring.strongbox.domain.FileContent;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.restassured.module.mockmvc.response.ValidatableMockMvcResponse;
import io.restassured.response.ResponseBodyExtractionOptions;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.carlspring.strongbox.controllers.logging.LoggingManagementController.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Pablo Tirado
 * @author Aditya Srinivasan
 * @author Przemyslaw Fusik
 */
@IntegrationTest
public class LoggingManagementControllerTest
        extends RestAssuredBaseTest
{

    public static final String LOG_FILE_BASE_NAME = "test-logfile-";

    @Inject
    private PropertiesBooter propertiesBooter;

    @Inject
    private WebApplicationContext context;

    private MockMvc pureMockMvc;

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();

        setContextBaseUrl(getContextBaseUrl() + ROOT_CONTEXT);

        pureMockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @WithMockUser(authorities = { "ADMIN" })
    public void testDownloadLog()
            throws Exception
    {
        String testLogName = "strongbox-test.log";
        Path file = Paths.get(propertiesBooter.getLogsDirectory(), testLogName);

        Files.createFile(file)
             .toFile()
             .deleteOnExit();

        // write some log messages.
        List<String> lines = Arrays.asList("Strongbox", "is", "awesome!");
        Files.write(file, lines, StandardCharsets.UTF_8);

        // Make sure the log file contains something.
        assertThat(file.toFile().length()).isGreaterThan(0);

        String url = getContextBaseUrl() + "/download/" + testLogName;

        ResponseBodyExtractionOptions body = mockMvc.header(HttpHeaders.ACCEPT,
                                                            MediaType.APPLICATION_OCTET_STREAM_VALUE)
                                                    .when()
                                                    .get(url)
                                                    .peek() // Use peek() to print the output
                                                    .then()
                                                    .statusCode(HttpStatus.OK.value()) // check http status code
                                                    .extract()
                                                    .body();

        assertThat(body.asByteArray().length).isEqualTo(file.toFile().length());
    }

    @WithMockUser(authorities = { "ADMIN" })
    @ParameterizedTest
    @ValueSource(strings = { "",
                             "test/sub/path1/" })
    public void testBrowseLogsJSON(String path)
    {
        int expectedLogsCount = 4;
        Path[] paths = createTestAssets(expectedLogsCount, path);
        assertThat(paths).as("Failed to create the right amount of log files").hasSize(expectedLogsCount);

        final String browseUrl = getContextBaseUrl() + BROWSE_BASE_PATH + "/" + path;
        final String downloadUrl = DOWNLOAD_BASE_PATH + "/" + path;

        try
        {
            ValidatableMockMvcResponse response = mockMvc.accept(MediaType.APPLICATION_JSON_VALUE)
                                                         .when()
                                                         .get(browseUrl)
                                                         .peek()
                                                         .then()
                                                         .statusCode(HttpStatus.OK.value());

            DirectoryListing listing = response.extract().as(DirectoryListing.class);
            assertThat(listing).isNotNull();
            assertThat(listing.getDirectories()).hasSizeGreaterThanOrEqualTo(0);
            assertThat(listing.getFiles()).hasSizeGreaterThanOrEqualTo(0);

            List<FileContent> logs = listing.getFiles()
                                            .stream()
                                            .filter(f -> f.getName().startsWith(LOG_FILE_BASE_NAME))
                                            .collect(Collectors.toList());

            assertThat(logs).hasSize(expectedLogsCount);

            FileContent firstFile = logs.get(0);
            assertThat(firstFile).isNotNull();
            assertThat(firstFile.getUrl()).isNotNull();
            assertThat(firstFile.getUrl().toString()).as("Invalid download link provided.").endsWith(downloadUrl + firstFile.getName());
        }
        finally
        {
            // Delete the temporary log files even if the test fails
            cleanTestAssets(path);
        }
    }

    @WithMockUser(authorities = { "ADMIN" })
    @ParameterizedTest
    @ValueSource(strings = { "",
                             "test/sub/path2/" })
    public void testBrowseLogsHTML(String path)
    {
        int expectedLogsCount = 4;
        Path[] paths = createTestAssets(expectedLogsCount, path);
        assertThat(paths).as("Failed to create the right amount of log files").hasSize(expectedLogsCount);

        final String browseUrl = getContextBaseUrl() + BROWSE_BASE_PATH + "/" + path;
        final String downloadUrl = DOWNLOAD_BASE_PATH + "/" + path;

        try
        {
            ValidatableMockMvcResponse response = mockMvc.accept(MediaType.TEXT_HTML_VALUE)
                                                         .when()
                                                         .get(browseUrl)
                                                         .peek()
                                                         .then()
                                                         .statusCode(HttpStatus.OK.value());

            String html = response.extract().asString();

            assertThat(StringUtils.countMatches(html, "class=\"directory\"")).isGreaterThanOrEqualTo(0);
            assertThat(StringUtils.countMatches(html, "class=\"file\"")).isGreaterThanOrEqualTo(expectedLogsCount);

            Pattern pattern = Pattern.compile(LOG_FILE_BASE_NAME + ".*<\\/a");
            Matcher matcher = pattern.matcher(html);
            // TODO: use matcher.results().count() when we switch to JDK11 as minimum version.
            int matches = 0;
            while (matcher.find())
            {
                matches++;
            }
            assertThat(matches).isEqualTo(expectedLogsCount);

            String expectedDownloadUrl = downloadUrl + paths[0].getFileName().toString();
            assertThat(html).as("Invalid download link provided.").contains(expectedDownloadUrl);

            // if this is a subpath we need to have a `show back` link.
            if (StringUtils.isNotBlank(path))
            {
                final String backUrl = DOWNLOAD_BASE_PATH + "/" + sanitizePath(Paths.get(path).getParent().toString());
                assertThat(html).as("Missing back link").contains("class=\"showBack\"");
                assertThat(html).as("Invalid back link").contains(backUrl);
            }
            else
            {
                assertThat(html).as("Should not have back link").doesNotContain("class=\"showBack\"");
            }
        }
        finally
        {
            // Delete the temporary log files even if the test fails
            cleanTestAssets(path);
        }
    }

    @Test
    void logFileStreamAndEmitsAsyncTest()
            throws Exception
    {
        final Logger loggingManagementControllerLogger = LoggerFactory.getLogger(LoggingManagementController.class);

        final MvcResult mvcResult = pureMockMvc.perform(get(ROOT_CONTEXT + "/stream"))
                                               .andExpect(request().asyncStarted())
                                               .andDo(MockMvcResultHandlers.log())
                                               .andReturn();

        loggingManagementControllerLogger.info("Wow! this is server-sent event test");


        pureMockMvc.perform(asyncDispatch(mvcResult))
                   .andDo(MockMvcResultHandlers.log())
                   .andExpect(status().isOk())
                   .andExpect(
                           content().contentType(org.carlspring.strongbox.net.MediaType.TEXT_EVENT_STREAM_UTF8_VALUE));

        final String response = mvcResult.getResponse().getContentAsString();

        assertThat(response).isNotEmpty();
    }

    /**
     * This method creates testing logging related assets (files and directories) to allow for testing directory listing
     * and downloading.
     *
     * @param logs
     * @param path
     *
     * @return
     */
    private Path[] createTestAssets(int logs,
                                    String path)
    {
        path = sanitizePath(path);

        //If a test directory is needed, a new directory called `test` under `/logs/` will be created.
        //Otherwise the path of `/logs` will be returned.
        Path logDirectoryPath;
        Path[] paths = new Path[logs];
        try
        {
            if (StringUtils.isNotBlank(path))
            {
                logDirectoryPath = Paths.get(propertiesBooter.getLogsDirectory(), path);
                Files.createDirectories(logDirectoryPath);
            }
            else
            {
                logDirectoryPath = Paths.get(propertiesBooter.getLogsDirectory());
            }

            //Create temporary log files
            for (int i = 0; i < logs; i++)
            {
                String name = LOG_FILE_BASE_NAME + i;
                paths[i] = Files.createTempFile(logDirectoryPath, name, ".log");
                Files.write(paths[i], (name + ".log").getBytes());
            }
        }
        catch (IOException e)
        {
            fail("Unable to create test log files and/or directories!");
        }

        return paths;
    }

    //This method deletes temporary log files, and if used for subdirectory browsing, the test log subdirectory.
    private void cleanTestAssets(String path)
    {
        path = sanitizePath(path);

        Path rootPath = Paths.get(propertiesBooter.getLogsDirectory());
        Path subPath = Paths.get(propertiesBooter.getLogsDirectory(), path);

        try
        {
            // create a stream
            Stream<Path> files = Files.walk(subPath);

            // delete directory including files and sub-folders
            files.sorted(Comparator.reverseOrder())
                 .filter(f -> !rootPath.equals(f) && f.getFileName().toString().contains(LOG_FILE_BASE_NAME))
                 .map(Path::toFile)
                 .forEach(File::delete);

            if (!rootPath.equals(subPath))
            {
                subPath.toFile().delete();
            }

            // close the stream
            files.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            fail("Unable to delete test log files and/or directories in [%s]!", subPath);
        }
    }

    private String sanitizePath(String path)
    {
        // Sanitize subDirectory
        if (StringUtils.isBlank(path))
        {
            path = "";
        }

        path = path.replaceAll("\\\\", "/");
        path = StringUtils.removeStart(path, "/");

        return path;
    }

}
