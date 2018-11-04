package org.carlspring.strongbox.cron.controller;

import org.carlspring.strongbox.cron.context.CronTaskRestTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.jobs.MyTask;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Alex Oreshkevich
 */
@CronTaskRestTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
public class CronTaskConfigurationControllerTest
        extends RestAssuredBaseTest
{

    private final String cronName1 = "CRJ001";

    private final String cronName2 = "CRJG001";

    @Override
    @BeforeEach
    public void init()
        throws Exception
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/api/configuration/crontasks");
    }

    @Test
    public void putDownloadRemoteMavenIndexCronJob()
        throws Exception
    {
        assertTrue(context.containsBean("repositoryIndexManager"));

        List<CronTaskConfigurationDto> configurationList = getDownloadRemoteMavenIndexOfCarlspringCronJobs();

        assertThat(configurationList.size(), CoreMatchers.equalTo(1));
        CronTaskConfigurationDto configuration = configurationList.get(0);
        assertThat(configuration.getProperties().keySet().size(), CoreMatchers.equalTo(4));
        assertThat(configuration.getProperties().get("cronExpression"), CoreMatchers.equalTo("0 0 5 * * ?"));

        configuration.addProperty("cronExpression", "0 0 0 * * ?");

        client.put2(getContextBaseUrl() + "/cron", configuration,
                    MediaType.APPLICATION_JSON_VALUE);

        configurationList = getDownloadRemoteMavenIndexOfCarlspringCronJobs();
        assertThat(configurationList.size(), CoreMatchers.equalTo(1));
        configuration = configurationList.get(0);
        assertThat(configuration.getProperties().keySet().size(), CoreMatchers.equalTo(4));
        assertThat(configuration.getProperties().get("cronExpression"), CoreMatchers.equalTo("0 0 0 * * ?"));
    }

    private List<CronTaskConfigurationDto> getDownloadRemoteMavenIndexOfCarlspringCronJobs()
    {

        final CronTasksConfigurationDto cronTasksConfiguration = given().accept(MediaType.APPLICATION_XML_VALUE)
                                                                        .when()
                                                                        .get(getContextBaseUrl() + "/")
                                                                        .peek()
                                                                        .as(CronTasksConfigurationDto.class);

        return cronTasksConfiguration.getCronTaskConfigurations()
                                     .stream()
                                     .filter(
                                             p -> "org.carlspring.strongbox.cron.jobs.DownloadRemoteMavenIndexCronJob".equals(
                                                                                                                              p.getRequiredProperty("jobClass")))
                                     .filter(
                                             p -> "storage-common-proxies".equals(p.getProperty("storageId")))
                                     .filter(
                                             p -> "carlspring".equals(p.getProperty("repositoryId")))
                                     .collect(Collectors.toList());
    }

    @Test
    public void testJavaCronTaskConfiguration()
        throws Exception
    {
        saveJavaConfig("0 11 11 11 11 ? 2100");

        // Remove comments to test cron job execution
        // saveJavaConfig("0 0/2 * 1/1 * ? *");

        deleteConfig(cronName1);
    }

    @Test
    public void testGroovyCronTaskConfiguration()
        throws Exception
    {
        saveGroovyConfig("0 11 11 11 11 ? 2100", cronName2);
        uploadGroovyScript();

        // Remove comments to test cron job execution *
        // listOfGroovyScriptsName();
        // saveGroovyConfig("0 0/2 * 1/1 * ? *");

        deleteConfig(cronName2);
    }

    public void saveJavaConfig(String cronExpression)
        throws UnsupportedEncodingException,
        JAXBException
    {
        logger.debug("Cron Expression: " + cronExpression);

        String url = "/cron";

        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setOneTimeExecution(true);
        configuration.setName(cronName1);
        configuration.addProperty("cronExpression", cronExpression);
        configuration.addProperty("jobClass", MyTask.class.getName());
        configuration.setOneTimeExecution(false);

        MockMvcResponse response = client.put2(getContextBaseUrl() + url,
                                               configuration,
                                               MediaType.APPLICATION_JSON_VALUE);

        int status = response.getStatusCode();
        if (OK != status)
        {
            logger.error(status + " | " + response.getStatusLine());
        }

        assertEquals(OK, status, "Failed to schedule job!");

        /**
         * Retrieve saved config
         */
        response = getCronConfig(cronName1);

        assertEquals(OK, response.getStatusCode(), "Failed to get cron task config! " + response.getStatusLine());

        logger.debug("Retrieved config " + response.getBody().asString());
    }

    public void saveGroovyConfig(String cronExpression,
                                 String name)
        throws UnsupportedEncodingException,
        JAXBException
    {
        logger.debug("Cron Expression: " + cronExpression);

        String url = client.getContextBaseUrl() + "/cron";

        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setOneTimeExecution(true);
        configuration.setName(name);
        configuration.addProperty("cronExpression", cronExpression);

        MockMvcResponse response = client.put2(url, configuration, MediaType.APPLICATION_JSON_VALUE);

        assertEquals(OK, response.getStatusCode(), "Failed to schedule job: " + response.getStatusLine());

        // Retrieve saved config
        response = getCronConfig(name);

        assertEquals(OK, response.getStatusCode(), "Failed to get cron task config!");

    }

    public void uploadGroovyScript()
        throws Exception
    {
        String fileName = "GroovyTask.groovy";
        File file = new File("target/test-classes/groovy/" + fileName);

        String path = getContextBaseUrl() + "/cron/groovy?cronName=" + cronName2;

        client.put(new FileInputStream(file), path, fileName, MediaType.APPLICATION_OCTET_STREAM_VALUE);
    }

    /**
     * Retrieve list of Groovy script file names
     */
    public void listOfGroovyScriptsName()
    {
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/groovy/names")
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    public void deleteConfig(String cronName)
    {
        MockMvcResponse response = deleteCronConfig(cronName);

        assertEquals(OK, response.getStatusCode(), "Failed to deleteCronConfig job: " + response.getStatusLine());

        // Retrieve deleted config
        response = getCronConfig(cronName);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode(), "Cron task config exists!");
    }

    private MockMvcResponse deleteCronConfig(String name)
    {
        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                      .param("name", name)
                      .when()
                      .delete(getContextBaseUrl() + "/cron")
                      .peek();
    }

    private MockMvcResponse getCronConfig(String name)
    {
        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                      .param("name", name)
                      .when()
                      .get(getContextBaseUrl() + "/cron")
                      .peek();
    }

}
