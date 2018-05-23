package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.MutableStorage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertNotNull;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class StrongboxConfigurationControllerTestIT
        extends RestAssuredBaseTest
{

    @Test
    public void testGetAndSetConfiguration()
    {
        MutableConfiguration configuration = getConfigurationFromRemote();

        MutableStorage storage = new MutableStorage("storage3");

        configuration.addStorage(storage);

        String url = getContextBaseUrl() + "/api/configuration/strongbox/xml";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(configuration)
               .when()
               .put(url)
               .then()
               .statusCode(200);

        final MutableConfiguration c = getConfigurationFromRemote();

        assertNotNull("Failed to create storage3!", c.getStorage("storage3"));
    }

    public MutableConfiguration getConfigurationFromRemote()
    {
        String url = getContextBaseUrl() + "/api/configuration/strongbox/xml";

        return given().contentType(MediaType.TEXT_PLAIN_VALUE)
                      .when()
                      .get(url)
                      .as(MutableConfiguration.class);
    }

}
