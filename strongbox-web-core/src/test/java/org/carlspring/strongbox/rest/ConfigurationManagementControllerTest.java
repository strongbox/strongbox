package org.carlspring.strongbox.rest;


import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.configuration.ServerConfiguration;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.jayway.restassured.response.ExtractableResponse;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.HttpServerErrorException;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by yury on 8/9/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
public class ConfigurationManagementControllerTest
        extends BackendBaseTest
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementControllerTest.class);
    @Inject
    ObjectMapper objectMapper;
    private RestClient client = RestClient.getTestInstanceLoggedInAsAdmin();

    @After
    public void tearDown()
            throws Exception
    {
        if (client != null)
        {
            client.close();
        }
    }

    @Test
    @WithUserDetails("admin")
    public void testSetAndGetPort()
            throws Exception
    {
        int newPort = 18080;

        String url = getContextBaseUrl() + "/configuration/strongbox/port/" + newPort;

        int status = RestAssuredMockMvc.given()
                                       .contentType("application/json")
                                       .when()
                                       .put(url)
                                       .peek() // Use peek() to print the ouput
                                       .then()
                                       .statusCode(200) // check http status code
                                       .extract()
                                       .statusCode();

        url = getContextBaseUrl() + "/configuration/strongbox/port";

        String port = RestAssuredMockMvc.given()
                                        .contentType("application/json")
                                        .when()
                                        .get(url)
                                        .peek() // Use peek() to print the ouput
                                        .then()
                                        .statusCode(200) // check http status code
                                        .extract().asString();

        assertEquals("Failed to set port!", 200, status);
        assertEquals("Failed to get port!", newPort, Integer.parseInt(port));

    }

    @Test
    @Ignore
    @WithUserDetails("admin")
    public void testSetAndGetBaseUrl()
            throws Exception
    {
        String baseUrl = "http://localhost:" + 40080 + "/newurl";

        //    int status = client.setBaseUrl(baseUrl);

        String url = getContextBaseUrl() + "/configuration/strongbox/baseUrl/" + baseUrl;

        //  Response response = resource.request(MediaType.TEXT_PLAIN).put(Entity.entity(baseUrl, MediaType.TEXT_PLAIN));

        //  return response.getStatus();
        System.out.println(url);
        int status = RestAssuredMockMvc.given()
                                       .contentType("application/json")
                                       .when()
                                       .put(getContextBaseUrl() + "/configuration/strongbox/baseUrl/" + baseUrl)
                                       .peek() // Use peek() to print the ouput
                                       .then()
                                       .statusCode(200) // check http status code
                                       .extract().statusCode();

        assertEquals("Failed to set baseUrl!", 200, status);

        //   String b = client.getBaseUrl();

        //   assertEquals("Failed to get baseUrl!", baseUrl, b);
    }

    @Test
    @WithUserDetails("admin")
    public void testSetAndGetGlobalProxyConfiguration()
            throws Exception
    {
        List<String> nonProxyHosts = new ArrayList<>();
        nonProxyHosts.add("localhost");
        nonProxyHosts.add("some-hosts.com");

        ProxyConfiguration proxyConfiguration = createProxyConfiguration();
        proxyConfiguration.setNonProxyHosts(nonProxyHosts);

        System.out.print("        -------------?????????????????----------->  " + proxyConfiguration);

        String url = getContextBaseUrl() + "/configuration/strongbox/proxy-configuration";

        RestAssuredMockMvc.given()
                          .contentType(MediaType.APPLICATION_XML_VALUE)
                          .body(proxyConfiguration, ObjectMapperType.JAXB)
                          .when()
                          .put(url)
                          .peek() // Use peek() to print the ouput
                          .then()
                          .statusCode(200);


        url = getContextBaseUrl() + "/configuration/strongbox/proxy-configuration";

        RestAssuredMockMvc.given()
                          .contentType(MediaType.APPLICATION_XML_VALUE)
                          .when()
                          .get(url)
                          .peek() // Use peek() to print the ouput
                          .then()
                          .statusCode(200)
                          .body("host", equalTo(proxyConfiguration.getHost()))
                          .body("port", equalTo(proxyConfiguration.getPort()))
                          .body("username", equalTo(proxyConfiguration.getUsername()))
                          .body("password", equalTo(proxyConfiguration.getPassword()))
                          .body("type", equalTo(proxyConfiguration.getType()))
                          .body("nonProxyHosts", equalTo(proxyConfiguration.getNonProxyHosts()))
                          .extract();
    }


    @Test
    @WithUserDetails("admin")
    public void testAddGetStorage()
            throws Exception
    {
        String storageId = "storage1";

        Storage storage1 = new Storage("storage1");

        String url = getContextBaseUrl() + "/configuration/strongbox/storages";

        RestAssuredMockMvc.given()
                          .contentType(MediaType.APPLICATION_XML_VALUE)
                          .body(storage1, ObjectMapperType.JAXB)
                          .when()
                          .put(url)
                          .peek() // Use peek() to print the ouput
                          .then()
                          .statusCode(200);

        Repository r1 = new Repository("repository0");
        r1.setAllowsRedeployment(true);
        r1.setSecured(true);
        r1.setStorage(storage1);

        Repository r2 = new Repository("repository1");
        r2.setAllowsForceDeletion(true);
        r2.setTrashEnabled(true);
        r2.setStorage(storage1);
        r2.setProxyConfiguration(createProxyConfiguration());

        addRepository(r1);
        addRepository(r2);

        url = getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId;

        RestAssuredMockMvc.given()
                          .contentType(MediaType.APPLICATION_XML_VALUE)
                          .when()
                          .get(url)
                          .peek() // Use peek() to print the ouput
                          .then()
                          .statusCode(200)

                          .extract();

        Storage storage = null;


        assertNotNull("Failed to get storage (" + storageId + ")!", storage);
        assertFalse("Failed to get storage (" + storageId + ")!", storage.getRepositories().isEmpty());
        assertTrue("Failed to get storage (" + storageId + ")!",
                   storage.getRepositories().get("repository0").allowsRedeployment());
        assertTrue("Failed to get storage (" + storageId + ")!",
                   storage.getRepositories().get("repository0").isSecured());
        assertTrue("Failed to get storage (" + storageId + ")!",
                   storage.getRepositories().get("repository1").allowsForceDeletion());
        assertTrue("Failed to get storage (" + storageId + ")!",
                   storage.getRepositories().get("repository1").isTrashEnabled());

        assertNotNull("Failed to get storage (" + storageId + ")!",
                      storage.getRepositories().get("repository1").getProxyConfiguration().getHost());
        assertEquals("Failed to get storage (" + storageId + ")!",
                     "localhost",
                     storage.getRepositories().get("repository1").getProxyConfiguration().getHost());
    }

    public int addRepository(Repository repository)
            throws IOException, JAXBException
    {
        String url;
        if (repository == null)
        {
            logger.error("Unable to add non-existing repository.");
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                               "Unable to add non-existing repository.");
        }

        if (repository.getStorage() == null)
        {
            logger.error("Storage associated with repo is null.");
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                               "Storage associated with repo is null.");
        }

        try
        {
            url = getContextBaseUrl() + "/configuration/strongbox/storages/" + repository.getStorage().getId() + "/" +
                  repository.getId();
        }
        catch (RuntimeException e)
        {
            logger.error("Unable to create web resource.", e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        int status = RestAssuredMockMvc.given()
                                       .contentType(MediaType.APPLICATION_XML_VALUE)
                                       .body(repository, ObjectMapperType.JAXB)
                                       .when()
                                       .put(url)
                                       .peek() // Use peek() to print the ouput
                                       .then()
                                       .statusCode(200)
                                       .extract().statusCode();

        return status;
    }

    @Test
    public void testCreateAndDeleteStorage()
            throws IOException, JAXBException
    {
        final String storageId = "storage2";
        final String repositoryId1 = "repository0";
        final String repositoryId2 = "repository1";

        Storage storage2 = new Storage(storageId);

        int response = client.addStorage(storage2);

        assertEquals("Failed to create storage!", 200, response);

        Repository r1 = new Repository(repositoryId1);
        r1.setAllowsRedeployment(true);
        r1.setSecured(true);
        r1.setStorage(storage2);
        r1.setProxyConfiguration(createProxyConfiguration());

        Repository r2 = new Repository(repositoryId2);
        r2.setAllowsRedeployment(true);
        r2.setSecured(true);
        r2.setStorage(storage2);

        client.addRepository(r1);
        client.addRepository(r2);

        final ProxyConfiguration pc = client.getProxyConfiguration(storageId, repositoryId1);

        assertNotNull("Failed to get proxy configuration!", pc);
        assertEquals("Failed to get proxy configuration!", pc.getHost(), pc.getHost());
        assertEquals("Failed to get proxy configuration!", pc.getPort(), pc.getPort());
        assertEquals("Failed to get proxy configuration!", pc.getUsername(), pc.getUsername());
        assertEquals("Failed to get proxy configuration!", pc.getPassword(), pc.getPassword());
        assertEquals("Failed to get proxy configuration!", pc.getType(), pc.getType());

        Storage storage = client.getStorage(storageId);

        assertNotNull("Failed to get storage (" + storageId + ")!", storage);
        assertFalse("Failed to get storage (" + storageId + ")!", storage.getRepositories().isEmpty());

        response = client.deleteRepository(storageId, repositoryId1, true);

        assertEquals("Failed to delete repository " + storageId + ":" + repositoryId1 + "!", 200, response);

        final Repository r = client.getRepository(storageId, repositoryId1);

        assertNull(r);

        File storageDir = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/" + storage.getId());

        assertTrue("Storage doesn't exist!", storageDir.exists());

        response = client.deleteStorage(storageId, true);

        assertEquals("Failed to delete storage " + storageId + "!", 200, response);

        final Storage s = client.getStorage(storageId);

        assertNull("Failed to delete storage " + storageId + "!", s);

        assertFalse("Failed to delete storage!", storageDir.exists());
    }

    @Test
    @WithUserDetails("admin")
    public void testGetAndSetConfiguration()
            throws IOException, JAXBException
    {
        //  final Configuration configuration = client.getConfiguration();
        String path = "/configuration/strongbox/xml";
        String url = getContextBaseUrl() + path;

        ExtractableResponse response1 = RestAssuredMockMvc.given()
                                                          .contentType(ContentType.JSON)
                                                          .when()
                                                          .get(url)
                                                          .peek() // Use peek() to print the ouput
                                                          .then()
                                                          .statusCode(200) // check http status code
                                                          .extract();

        ServerConfiguration configuration1 = null;
        if (response1.statusCode() == 200)
        {
            final String xml = response1.asString();

            final ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());

            GenericParser<ServerConfiguration> parser = new GenericParser<>(Configuration.class);

            configuration1 = parser.parse(bais);
        }

        final Configuration configuration = (Configuration) configuration1;


        Storage storage = new Storage("storage3");

        configuration.addStorage(storage);

        final int response = client.setConfiguration(configuration);

        /*Response response = resource.request(MediaType.TEXT_PLAIN_TYPE)
                                    .put(Entity.entity(baos.toString("UTF-8"), MediaType.APPLICATION_XML));

        return response.getStatus();*/

        int status = RestAssuredMockMvc.given()
                                       .contentType(ContentType.XML)
                                       .when()
                                       .put(url)
                                       .peek() // Use peek() to print the ouput
                                       .then()
                                       .statusCode(200) // check http status code
                                       .extract().statusCode();


        assertEquals("Failed to retrieve configuration!", 200, status);

        ExtractableResponse response2 = RestAssuredMockMvc.given()
                                                          .contentType(ContentType.XML)
                                                          .when()
                                                          .get(url)
                                                          .peek() // Use peek() to print the ouput
                                                          .then()
                                                          .statusCode(200) // check http status code
                                                          .extract();

        ServerConfiguration configuration2 = null;
        if (response2.statusCode() == 200)
        {
            final String xml = response2.asString();

            final ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());

            GenericParser<ServerConfiguration> parser = new GenericParser<>(Configuration.class);

            configuration2 = parser.parse(bais);
        }

        final Configuration c = (Configuration) configuration2;

        //  final Configuration c = client.getConfiguration();

        assertNotNull("Failed to create storage3!", c.getStorage("storage3"));
    }

    private ProxyConfiguration createProxyConfiguration()
    {
        ProxyConfiguration proxyConfiguration = new ProxyConfiguration();
        proxyConfiguration.setHost("localhost");
        proxyConfiguration.setPort(8080);
        proxyConfiguration.setUsername("user1");
        proxyConfiguration.setPassword("pass2");
        proxyConfiguration.setType("http");

        return proxyConfiguration;
    }

}
