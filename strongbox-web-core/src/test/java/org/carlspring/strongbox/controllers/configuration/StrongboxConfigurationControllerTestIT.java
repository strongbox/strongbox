package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.StorageDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.net.MediaType.APPLICATION_YAML_VALUE;

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

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             APPLICATION_YAML_VALUE })
    public void testGetAndSetConfiguration(String acceptHeader)
    {
        MutableConfiguration configuration = getConfigurationFromRemote();

        StorageDto storage = new StorageDto("storage3");

        configuration.addStorage(storage);

        String url = getContextBaseUrl() + "/api/configuration/strongbox";

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(acceptHeader)
                     .body(configuration)
                     .when()
                     .put(url)
                     .then()
                     .statusCode(200);

        final MutableConfiguration c = getConfigurationFromRemote();

        assertThat(c.getStorage("storage3"))
                .as("Failed to create storage3!")
                .isNotNull();
    }

    public MutableConfiguration getConfigurationFromRemote()
    {
        String url = getContextBaseUrl() + "/api/configuration/strongbox";

        return givenCustom().accept(MediaType.APPLICATION_JSON_VALUE)
                            .when()
                            .get(url)
                            .as(MutableConfiguration.class);
    }

}
