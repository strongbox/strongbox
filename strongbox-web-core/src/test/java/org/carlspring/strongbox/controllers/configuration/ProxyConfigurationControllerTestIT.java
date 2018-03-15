package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class ProxyConfigurationControllerTestIT
        extends RestAssuredBaseTest
{

    static ProxyConfiguration createProxyConfiguration()
    {
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration();
        proxyConfiguration.setHost("localhost");
        proxyConfiguration.setPort(8080);
        proxyConfiguration.setUsername("user1");
        proxyConfiguration.setPassword("pass2");
        proxyConfiguration.setType("http");
        List<String> nonProxyHosts = new ArrayList<>();
        nonProxyHosts.add("localhost");
        nonProxyHosts.add("some-hosts.com");
        proxyConfiguration.setNonProxyHosts(nonProxyHosts);

        return proxyConfiguration;
    }

    @Test
    public void testSetAndGetGlobalProxyConfiguration()
    {
        ProxyConfiguration proxyConfiguration = createProxyConfiguration();

        String url = getContextBaseUrl() + "/api/configuration/strongbox/proxy-configuration";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(proxyConfiguration)
               .when()
               .put(url)
               .then()
               .statusCode(200);

        url = getContextBaseUrl() + "/api/configuration/strongbox/proxy-configuration";

        logger.debug("Current proxy host: " + proxyConfiguration.getHost());

        ProxyConfiguration pc = given().contentType(MediaType.APPLICATION_XML_VALUE)
                                       .when()
                                       .get(url)
                                       .as(ProxyConfiguration.class);

        assertNotNull("Failed to get proxy configuration!", pc);
        assertEquals("Failed to get proxy configuration!", proxyConfiguration.getHost(), pc.getHost());
        assertEquals("Failed to get proxy configuration!", proxyConfiguration.getPort(), pc.getPort());
        assertEquals("Failed to get proxy configuration!", proxyConfiguration.getUsername(), pc.getUsername());
        assertEquals("Failed to get proxy configuration!", proxyConfiguration.getPassword(), pc.getPassword());
        assertEquals("Failed to get proxy configuration!", proxyConfiguration.getType(), pc.getType());
        assertEquals("Failed to get proxy configuration!", proxyConfiguration.getNonProxyHosts(),
                     pc.getNonProxyHosts());
    }
}
