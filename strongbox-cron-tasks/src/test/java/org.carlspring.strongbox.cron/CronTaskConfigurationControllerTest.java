package org.carlspring.strongbox.cron;

import org.carlspring.strongbox.cron.context.CronTaskTest;
import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.exceptions.CronTaskNotFoundException;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;

import com.jayway.restassured.module.mockmvc.response.MockMvcResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SchedulerException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.junit.Assert.assertEquals;

/**
 * @author Alex Oreshkevich
 */
@CronTaskTest
@RunWith(SpringJUnit4ClassRunner.class)
public class CronTaskConfigurationControllerTest
        extends RestAssuredBaseTest
{

    private final String cronName1 = "CRJ001";
    private final String cronName2 = "CRJG001";

    @Override
    public void init()
    {
        super.init();
        setContextBaseUrl(getContextBaseUrl() + "/configuration/crontasks");
    }

    @Test
    public void testJavaCronTaskConfiguration()
            throws ClassNotFoundException,
                   SchedulerException,
                   CronTaskNotFoundException,
                   UnsupportedEncodingException,
                   JAXBException
    {
        saveJavaConfig("0 0/1 * 1/1 * ? *");

        /**
         * Remove comments to test cron job execution
         * */
        // saveJavaConfig("0 0/2 * 1/1 * ? *");

        deleteConfig(cronName1);
    }

    public void saveJavaConfig(String cronExpression)
            throws UnsupportedEncodingException, JAXBException
    {
        logger.debug("Cron Expression: " + cronExpression);

        String url = "/cron";

        CronTaskConfiguration configuration = new CronTaskConfiguration();
        configuration.setName(cronName1);
        configuration.addProperty("cronExpression", cronExpression);
        configuration.addProperty("jobClass", MyTask.class.getName());

        MockMvcResponse response = client.put(getContextBaseUrl() + url, configuration,
                                              MediaType.APPLICATION_JSON_VALUE);

        int status = response.getStatusCode();
        if (OK != status)
        {
            logger.error(status + " | " + response.getStatusLine());
        }

        assertEquals("Failed to schedule job!", OK, status);

        System.out.println("\n\n ###### getCronConfig ##### \n\n");

        /**
         * Retrieve saved config
         * */
        response = getCronConfig(cronName1);

        assertEquals("Failed to get cron task config! " + response.getStatusLine(), OK, response.getStatusCode());
        logger.debug("Retrieved config " + response.getBody().asString());
    }

    public void deleteConfig(String cronName)
    {
        MockMvcResponse response = deleteCronConfig(cronName);
        assertEquals("Failed to deleteCronConfig job!", OK, response.getStatusCode());

        /**
         * Retrieve deleted config
         * */
        response = getCronConfig(cronName);

        assertEquals("Cron task config exists!",
                     HttpStatus.BAD_REQUEST.value(),
                     response.getStatusCode());
    }

    private MockMvcResponse deleteCronConfig(String name)
    {
        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                      .param("name", name)
                      .when()
                      .delete(getContextBaseUrl() + "/cron").peek();
    }

    private MockMvcResponse getCronConfig(String name)
    {
        return given().contentType(MediaType.APPLICATION_JSON_VALUE)
                      .param("name", name)
                      .when()
                      .get(getContextBaseUrl() + "/cron").peek();
    }


}
