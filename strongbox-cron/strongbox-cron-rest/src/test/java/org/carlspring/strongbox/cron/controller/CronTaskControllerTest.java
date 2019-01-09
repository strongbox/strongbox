package org.carlspring.strongbox.cron.controller;

import org.carlspring.strongbox.cron.context.CronTaskRestTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.jobs.MyTask;
import org.carlspring.strongbox.forms.cron.CronTaskConfigurationForm;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import io.restassured.module.mockmvc.response.MockMvcResponse;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.EnabledIf;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.cron.controller.CronTaskController.CRON_CONFIG_FILE_NAME_KEY;
import static org.carlspring.strongbox.cron.controller.CronTaskController.CRON_CONFIG_JOB_CLASS_KEY;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@CronTaskRestTest
@ExtendWith(SpringExtension.class)
@ActiveProfiles(profiles = "test")
public class CronTaskControllerTest
        extends RestAssuredBaseTest
{

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/configuration/crontasks");
    }

    @Test
    @EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
    public void createNewCronJob()
    {
        CronTaskConfigurationForm configurationForm = createForm();

        // Create cron job.
        createConfig(configurationForm);

        // Delete cron job.
        deleteConfig(configurationForm.getUuid());
    }

    @Test
    @EnabledIf(expression = "#{containsObject('repositoryIndexManager')}", loadContext = true)
    public void updateDownloadRemoteMavenIndexCronJob()
            throws Exception
    {
        final String currentCronExpression = "0 0 5 * * ?";
        final String newCronExpression = "0 0 0 * * ?";

        List<CronTaskConfigurationDto> configurationList = getDownloadRemoteMavenIndexOfCarlspringCronJobs();
        assertEquals(configurationList.size(), 1);

        CronTaskConfigurationDto configuration = configurationList.get(0);
        assertEquals(configuration.getProperties().keySet().size(), 4);
        assertEquals(configuration.getProperties().get("cronExpression"), currentCronExpression);

        configuration.addProperty("cronExpression", newCronExpression);

        CronTaskConfigurationForm configurationForm = convertToForm(configuration);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(configurationForm)
               .when()
               .put(getContextBaseUrl() + "/" + configuration.getUuid())
               .peek()
               .then()
               .statusCode(OK);

        configurationList = getDownloadRemoteMavenIndexOfCarlspringCronJobs();
        assertEquals(configurationList.size(), 1);

        configuration = configurationList.get(0);
        assertEquals(configuration.getProperties().keySet().size(), 4);
        assertEquals(configuration.getProperties().get("cronExpression"), newCronExpression);

        // Revert changes
        configuration.addProperty("cronExpression", currentCronExpression);
        configurationForm = convertToForm(configuration);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(configurationForm)
               .when()
               .put(getContextBaseUrl() + "/" + configuration.getUuid())
               .peek()
               .then()
               .statusCode(OK);
    }

    @Test
    public void testJavaCronTaskConfiguration()
    {
        final String cronUuid = "adf64aa4-fba4-11e8-8eb2-f2801f1b9fd1";
        final String cronName = "CRJ001";

        saveCronConfig("0 11 11 11 11 ? 2100", cronUuid, cronName, MyTask.class.getName());

        // Remove comments to test cron job execution
        // saveCronConfig("0 0/2 * 1/1 * ? *", cronUuid, cronName, MyTask.class.getName());

        deleteConfig(cronUuid);
    }

    @Test
    public void testGroovyCronTaskConfiguration()
            throws Exception
    {
        final String cronUuid = "b5a288db-3768-4c79-8cc9-ea6ea3af6f5a";
        final String cronName = "CRJG001";

        saveCronConfig("0 11 11 11 11 ? 2100", cronUuid, cronName, null);
        uploadGroovyScript(cronUuid);

        // Remove comments to test cron job execution *
        // listOfGroovyScriptsName();
        // saveCronConfig("0 0/2 * 1/1 * ? *", cronUuid, cronName, null);

        deleteConfig(cronUuid);
    }

    private CronTaskConfigurationForm createForm()
    {
        CronTaskConfigurationForm form = new CronTaskConfigurationForm();
        form.setUuid(UUID.randomUUID().toString());
        form.setName("Cron Job Test");
        form.addProperty("cronExpression", "0 0 0 * * ?");
        form.addProperty(CRON_CONFIG_JOB_CLASS_KEY, MyTask.class.getName());
        form.setOneTimeExecution(true);
        form.setImmediateExecution(true);

        return form;
    }

    private void createConfig(CronTaskConfigurationForm configurationForm)
    {
        MockMvcResponse response = createCronConfig(configurationForm);

        assertEquals(OK, response.getStatusCode(), "Failed to create cron config job: " + response.getStatusLine());

        // Retrieve created config
        response = getCronConfig(configurationForm.getUuid());

        assertEquals(OK, response.getStatusCode(), "Cron task config does not exist!");
    }

    private MockMvcResponse createCronConfig(CronTaskConfigurationForm configurationForm)
    {
        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                      .accept(MediaType.APPLICATION_JSON_VALUE)
                      .body(configurationForm)
                      .when()
                      .put(getContextBaseUrl() + "/")
                      .peek();
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
                                                     p.getRequiredProperty(CRON_CONFIG_JOB_CLASS_KEY)))
                                     .filter(
                                             p -> "storage-common-proxies".equals(p.getProperty("storageId")))
                                     .filter(
                                             p -> "carlspring".equals(p.getProperty("repositoryId")))
                                     .collect(Collectors.toList());
    }

    private CronTaskConfigurationForm convertToForm(CronTaskConfigurationDto configuration)
    {
        CronTaskConfigurationForm form = new CronTaskConfigurationForm();
        form.setUuid(configuration.getUuid());
        form.setName(configuration.getName());
        form.setProperties(configuration.getProperties());
        form.setOneTimeExecution(configuration.isOneTimeExecution());
        form.setImmediateExecution(configuration.shouldExecuteImmediately());

        return form;
    }


    private void saveCronConfig(String cronExpression,
                                String uuid,
                                String name,
                                String className)
    {
        logger.debug("Cron Expression: {}", cronExpression);

        CronTaskConfigurationForm configuration = new CronTaskConfigurationForm();
        configuration.setUuid(uuid);
        configuration.setName(name);
        configuration.addProperty("cronExpression", cronExpression);

        if (className != null)
        {
            configuration.addProperty(CRON_CONFIG_JOB_CLASS_KEY, className);
        }

        configuration.setOneTimeExecution(true);
        configuration.setImmediateExecution(false);

        MockMvcResponse response = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                                          .accept(MediaType.APPLICATION_JSON_VALUE)
                                          .body(configuration)
                                          .when()
                                          .put(getContextBaseUrl() + "/" + configuration.getUuid())
                                          .peek();

        int status = response.getStatusCode();
        if (OK != status)
        {
            logger.error(status + " | " + response.getStatusLine());
        }

        assertEquals(OK, status, "Failed to schedule job!");

        // Retrieve saved config
        response = getCronConfig(configuration.getUuid());

        assertEquals(OK, response.getStatusCode(), "Failed to get cron task config! " + response.getStatusLine());

        logger.debug("Retrieved config " + response.getBody().asString());
    }

    private void uploadGroovyScript(String uuid)
            throws Exception
    {
        String fileName = "GroovyTask.groovy";
        File file = new File("target/test-classes/groovy/" + fileName);

        String url = getContextBaseUrl() + "/cron/groovy/" + uuid;

        String contentDisposition = "attachment; filename=\"" + fileName + "\"";
        InputStream is = new FileInputStream(file);
        byte[] bytes = IOUtils.toByteArray(is);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
               .header(CRON_CONFIG_FILE_NAME_KEY, fileName)
               .body(bytes)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(OK);
    }

    /**
     * Retrieve list of Groovy script file names
     */
    private void listOfGroovyScriptsName()
    {
        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(getContextBaseUrl() + "/groovy/names")
               .peek()
               .then()
               .statusCode(OK);
    }

    private void deleteConfig(String cronUuid)
    {
        MockMvcResponse response = deleteCronConfig(cronUuid);

        assertEquals(OK, response.getStatusCode(), "Failed to deleteCronConfig job: " + response.getStatusLine());

        // Retrieve deleted config
        response = getCronConfig(cronUuid);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode(), "Cron task config exists!");
    }

    private MockMvcResponse deleteCronConfig(String uuid)
    {
        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                      .accept(MediaType.APPLICATION_JSON_VALUE)
                      .when()
                      .delete(getContextBaseUrl() + "/" + uuid)
                      .peek();
    }

    private MockMvcResponse getCronConfig(String uuid)
    {
        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                      .accept(MediaType.APPLICATION_JSON_VALUE)
                      .when()
                      .get(getContextBaseUrl() + "/" + uuid)
                      .peek();
    }

}
