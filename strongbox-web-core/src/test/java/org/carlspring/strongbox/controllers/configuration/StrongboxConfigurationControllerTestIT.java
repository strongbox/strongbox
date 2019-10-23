package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.StorageDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.net.MediaType.APPLICATION_YAML_VALUE;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;


/**
 * @author Pablo Tirado
 */
@ActiveProfiles({ "test",
                  "StrongboxConfigurationControllerTestIT" })
@IntegrationTest
@Execution(SAME_THREAD)
public class StrongboxConfigurationControllerTestIT
        extends RestAssuredBaseTest
{

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/configuration/strongbox");
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             APPLICATION_YAML_VALUE })
    public void testGetAndSetConfiguration(String acceptHeader)
    {
        final String storageId = "storage3";
        StorageDto storage = new StorageDto(storageId);

        MutableConfiguration configuration = getConfigurationFromRemote();
        configuration.addStorage(storage);

        String url = getContextBaseUrl();

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(acceptHeader)
                     .body(configuration)
                     .when()
                     .put(url)
                     .then()
                     .statusCode(HttpStatus.OK.value());

        final MutableConfiguration c = getConfigurationFromRemote();

        assertThat(c.getStorage("storage3"))
                .as("Failed to create storage3!")
                .isNotNull();
    }

    private MutableConfiguration getConfigurationFromRemote()
    {
        String url = getContextBaseUrl();

        return givenCustom().accept(MediaType.APPLICATION_JSON_VALUE)
                            .when()
                            .get(url)
                            .as(MutableConfiguration.class);
    }

}
