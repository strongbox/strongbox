package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.configuration.MutableProxyConfiguration;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import java.util.List;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertNotNull;
import static org.carlspring.strongbox.controllers.configuration.ProxyConfigurationController.FAILED_UPDATE;
import static org.carlspring.strongbox.controllers.configuration.ProxyConfigurationController.FAILED_UPDATE_FORM_ERROR;
import static org.carlspring.strongbox.controllers.configuration.ProxyConfigurationController.SUCCESSFUL_UPDATE;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class ProxyConfigurationControllerTestIT
        extends RestAssuredBaseTest
{

    @Before
    public void setUp()
    {
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

    private void testSetAndGetGlobalProxyConfiguration(String acceptHeader)
    {
        MutableProxyConfiguration proxyConfiguration = createProxyConfiguration();

        String url = getContextBaseUrl();

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(proxyConfiguration)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString(SUCCESSFUL_UPDATE));

        logger.debug("Current proxy host: " + proxyConfiguration.getHost());

        MutableProxyConfiguration pc = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                                              .accept(MediaType.APPLICATION_JSON_VALUE)
                                              .when()
                                              .get(url)
                                              .as(MutableProxyConfiguration.class);

        assertNotNull("Failed to get proxy configuration!", pc);
        assertEquals("Failed to get proxy configuration!", proxyConfiguration.getHost(), pc.getHost());
        assertEquals("Failed to get proxy configuration!", proxyConfiguration.getPort(), pc.getPort());
        assertEquals("Failed to get proxy configuration!", proxyConfiguration.getUsername(), pc.getUsername());
        assertEquals("Failed to get proxy configuration!", proxyConfiguration.getPassword(), pc.getPassword());
        assertEquals("Failed to get proxy configuration!", proxyConfiguration.getType(), pc.getType());
        assertEquals("Failed to get proxy configuration!", proxyConfiguration.getNonProxyHosts(),
                     pc.getNonProxyHosts());
    }

    @WithMockUser(authorities = {"CONFIGURATION_SET_GLOBAL_PROXY_CFG", "CONFIGURATION_VIEW_GLOBAL_PROXY_CFG"})
    @Test
    public void testSetAndGetGlobalProxyConfigurationWithTextAcceptHeader()
    {
        testSetAndGetGlobalProxyConfiguration(MediaType.TEXT_PLAIN_VALUE);
    }

    @WithMockUser(authorities = {"CONFIGURATION_SET_GLOBAL_PROXY_CFG", "CONFIGURATION_VIEW_GLOBAL_PROXY_CFG"})
    @Test
    public void testSetAndGetGlobalProxyConfigurationWithJsonAcceptHeader()
    {
        testSetAndGetGlobalProxyConfiguration(MediaType.APPLICATION_JSON_VALUE);
    }

    private void testSetGlobalProxyConfigurationNotFound(String acceptHeader)
    {
        MutableProxyConfiguration proxyConfiguration = createProxyConfiguration();

        String url = getContextBaseUrl();
        String storageId = "storage-not-found";
        String repositoryId = "repo-not-found";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
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
    @Test
    public void testSetGlobalProxyConfigurationNotFoundWithTextAcceptHeader()
    {
        testSetGlobalProxyConfigurationNotFound(MediaType.TEXT_PLAIN_VALUE);
    }

    @WithMockUser(authorities = "CONFIGURATION_SET_GLOBAL_PROXY_CFG")
    @Test
    public void testSetGlobalProxyConfigurationNotFoundWithJsonAcceptHeader()
    {
        testSetGlobalProxyConfigurationNotFound(MediaType.APPLICATION_JSON_VALUE);
    }

    private void testSetGlobalProxyConfigurationBadRequest(String acceptHeader)
    {
        MutableProxyConfiguration proxyConfiguration = createWrongProxyConfiguration();

        String url = getContextBaseUrl();

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(proxyConfiguration)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_UPDATE_FORM_ERROR));

    }

    @WithMockUser(authorities = "CONFIGURATION_SET_GLOBAL_PROXY_CFG")
    @Test
    public void testSetGlobalProxyConfigurationBadRequestWithTextAcceptHeader()
    {
        testSetGlobalProxyConfigurationBadRequest(MediaType.TEXT_PLAIN_VALUE);
    }

    @WithMockUser(authorities = "CONFIGURATION_SET_GLOBAL_PROXY_CFG")
    @Test
    public void testSetGlobalProxyConfigurationBadRequestWithJsonAcceptHeader()
    {
        testSetGlobalProxyConfigurationBadRequest(MediaType.APPLICATION_JSON_VALUE);
    }
}
