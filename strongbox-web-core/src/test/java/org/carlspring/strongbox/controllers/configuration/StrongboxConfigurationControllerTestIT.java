package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.MutableStorage;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class StrongboxConfigurationControllerTestIT
        extends RestAssuredBaseTest
{

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
    }

    @Test
    public void testGetAndSetConfiguration()
    {
        MutableConfiguration configuration = getConfigurationFromRemote();

        MutableStorage storage = new MutableStorage("storage3");

        configuration.addStorage(storage);

        String url = getContextBaseUrl() + "/api/configuration/strongbox";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(configuration)
               .when()
               .put(url)
               .then()
               .statusCode(200);

        final MutableConfiguration c = getConfigurationFromRemote();

        assertNotNull(c.getStorage("storage3"), "Failed to create storage3!");
    }

    public MutableConfiguration getConfigurationFromRemote()
    {
        String url = getContextBaseUrl() + "/api/configuration/strongbox";

        return given().accept(MediaType.APPLICATION_JSON_VALUE)
                      .when()
                      .get(url)
                      .as(MutableConfiguration.class);
    }

}
