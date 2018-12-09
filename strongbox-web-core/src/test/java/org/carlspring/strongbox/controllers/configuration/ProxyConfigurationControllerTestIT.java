package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.configuration.MutableProxyConfiguration;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import java.util.List;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.controllers.configuration.ProxyConfigurationController.FAILED_UPDATE;
import static org.carlspring.strongbox.controllers.configuration.ProxyConfigurationController.FAILED_UPDATE_FORM_ERROR;
import static org.carlspring.strongbox.controllers.configuration.ProxyConfigurationController.SUCCESSFUL_UPDATE;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * @author Pablo Tirado
 */
@IntegrationTest
public class ProxyConfigurationControllerTestIT
        extends RestAssuredBaseTest
{

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/configuration/strongbox/proxy-configuration");
    }

    static MutableProxyConfiguration createProxyConfiguration()
    {
        MutableProxyConfiguration proxyConfiguration = new MutableProxyConfiguration();
        proxyConfiguration.setHost("localhost");
        proxyConfiguration.setPort(8080);
        proxyConfiguration.setUsername("user1");
        proxyConfiguration.setPassword("pass2");
        proxyConfiguration.setType("http");
        List<String> nonProxyHosts = Lists.newArrayList();
        nonProxyHosts.add("localhost");
        nonProxyHosts.add("some-hosts.com");
        proxyConfiguration.setNonProxyHosts(nonProxyHosts);

        return proxyConfiguration;
    }

    private static MutableProxyConfiguration createWrongProxyConfiguration()
    {
        MutableProxyConfiguration proxyConfiguration = new MutableProxyConfiguration();
        proxyConfiguration.setHost("");
        proxyConfiguration.setPort(0);
        proxyConfiguration.setUsername("user1");
        proxyConfiguration.setPassword("pass2");
        proxyConfiguration.setType("TEST");
        List<String> nonProxyHosts = Lists.newArrayList();
        nonProxyHosts.add("localhost");
        nonProxyHosts.add("some-hosts.com");
        proxyConfiguration.setNonProxyHosts(nonProxyHosts);

        return proxyConfiguration;
    }

    @WithMockUser(authorities = {"CONFIGURATION_SET_GLOBAL_PROXY_CFG",
                                 "CONFIGURATION_VIEW_GLOBAL_PROXY_CFG"})
    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testSetAndGetGlobalProxyConfiguration(String acceptHeader)
    {
        MutableProxyConfiguration proxyConfiguration = createProxyConfiguration();

        String url = getContextBaseUrl();

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(proxyConfiguration)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_UPDATE));

        logger.debug("Current proxy host: {}", proxyConfiguration.getHost());

        MutableProxyConfiguration pc = mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .when()
                                              .get(url)
                                              .as(MutableProxyConfiguration.class);

        assertThat(pc).as("Failed to get proxy configuration!").isNotNull();
        assertThat(pc.getPort()).as("Failed to get proxy configuration!").isEqualTo(proxyConfiguration.getPort());
        assertThat(pc.getUsername()).as("Failed to get proxy configuration!").isEqualTo(proxyConfiguration.getUsername());
        assertThat(pc.getPassword()).as("Failed to get proxy configuration!").isEqualTo(proxyConfiguration.getPassword());
        assertThat(pc.getType()).as("Failed to get proxy configuration!").isEqualTo(proxyConfiguration.getType());
        assertThat(pc.getNonProxyHosts()).as("Failed to get proxy configuration!").isEqualTo(proxyConfiguration.getNonProxyHosts());
    }

    @WithMockUser(authorities = "CONFIGURATION_SET_GLOBAL_PROXY_CFG")
    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testSetGlobalProxyConfigurationNotFound(String acceptHeader)
    {
        MutableProxyConfiguration proxyConfiguration = createProxyConfiguration();

        String url = getContextBaseUrl();
        String storageId = "storage-not-found";
        String repositoryId = "repo-not-found";

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(proxyConfiguration)
               .param("storageId", storageId)
               .param("repositoryId", repositoryId)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
               .body(containsString(FAILED_UPDATE));
    }

    @WithMockUser(authorities = "CONFIGURATION_SET_GLOBAL_PROXY_CFG")
    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    void testSetGlobalProxyConfigurationBadRequest(String acceptHeader)
    {
        MutableProxyConfiguration proxyConfiguration = createWrongProxyConfiguration();

        String url = getContextBaseUrl();

        mockMvc.contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(proxyConfiguration)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_UPDATE_FORM_ERROR));

    }

}
