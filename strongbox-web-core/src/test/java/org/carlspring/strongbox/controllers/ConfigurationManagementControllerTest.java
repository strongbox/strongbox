package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.controllers.support.BaseUrlEntityBody;
import org.carlspring.strongbox.controllers.support.PortEntityBody;
import org.carlspring.strongbox.controllers.support.ResponseStatusEnum;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpServerErrorException;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class ConfigurationManagementControllerTest
        extends RestAssuredBaseTest
{

    @Test
    public void testSetAndGetPort()
            throws Exception
    {
        int newPort = 18080;

        String url = getContextBaseUrl() + "/configuration/strongbox/port/" + newPort;

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(equalTo(ResponseStatusEnum.OK.value()));

        url = getContextBaseUrl() + "/configuration/strongbox/port";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("port", equalTo(newPort));
    }

    @Test
    public void testSetAndGetPortWithBody()
            throws Exception
    {
        int newPort = 18080;
        PortEntityBody portEntity = new PortEntityBody(newPort);

        String url = getContextBaseUrl() + "/configuration/strongbox/port/" + newPort;

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(portEntity)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(equalTo(ResponseStatusEnum.OK.value()));

        url = getContextBaseUrl() + "/configuration/strongbox/port";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body("port", equalTo(newPort));
    }

    @Test
    public void testSetAndGetBaseUrl()
            throws Exception
    {
        String newBaseUrl = "http://localhost:" + 40080 + "/newurl";
        BaseUrlEntityBody baseUrlEntity = new BaseUrlEntityBody(newBaseUrl);

        String url = getContextBaseUrl() + "/configuration/strongbox/baseUrl";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(baseUrlEntity)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value()) // check http status code
               .body(equalTo(ResponseStatusEnum.OK.value()));

        url = getContextBaseUrl() + "/configuration/strongbox/baseUrl";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("baseUrl", equalTo(newBaseUrl));
    }

    @Test
    public void testSetAndGetGlobalProxyConfiguration()
            throws Exception
    {
        ProxyConfiguration proxyConfiguration = createProxyConfiguration();

        String url = getContextBaseUrl() + "/configuration/strongbox/proxy-configuration";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(proxyConfiguration)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(ResponseStatusEnum.OK.value()));

        url = getContextBaseUrl() + "/configuration/strongbox/proxy-configuration";

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

    @Test
    public void testAddGetStorage()
            throws Exception
    {
        String storageId = "storage1";

        Storage storage1 = new Storage("storage1");

        String url = getContextBaseUrl() + "/configuration/strongbox/storages";

        logger.debug("Using storage class " + storage1.getClass()
                                                      .getName());

        given().contentType(MediaType.APPLICATION_XML_VALUE)
               .body(storage1)
               .when()
               .put(url)
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(ResponseStatusEnum.OK.value()));

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

        Storage storage = getStorage(storageId);

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

    private Storage getStorage(String storageId)
    {

        String url = getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId;

        return given().contentType(MediaType.TEXT_PLAIN_VALUE)
                      .when()
                      .get(url)
                      .as(Storage.class);
    }

    private int addRepository(Repository repository)
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

        int status = given().contentType(MediaType.APPLICATION_XML_VALUE)
                            .body(repository)
                            .when()
                            .put(url)
                            .then()
                            .statusCode(200)
                            .extract()
                            .statusCode();

        return status;
    }

    @Test
    public void testCreateAndDeleteStorage()
    {
        final String storageId = "storage2";
        final String repositoryId1 = "repository0";
        final String repositoryId2 = "repository1";

        Storage storage2 = new Storage(storageId);

        String url = getContextBaseUrl() + "/configuration/strongbox/storages";

        given().contentType(MediaType.APPLICATION_XML_VALUE)
               .body(storage2)
               .when()
               .put(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(ResponseStatusEnum.OK.value()));

        Repository r1 = new Repository(repositoryId1);
        r1.setAllowsRedeployment(true);
        r1.setSecured(true);
        r1.setStorage(storage2);
        r1.setProxyConfiguration(createProxyConfiguration());

        Repository r2 = new Repository(repositoryId2);
        r2.setAllowsRedeployment(true);
        r2.setSecured(true);
        r2.setStorage(storage2);

        addRepository(r1);
        addRepository(r2);

        /*
        final ProxyConfiguration pc = client.getProxyConfiguration(storageId, repositoryId1);

        assertNotNull("Failed to get proxy configuration!", pc);
        assertEquals("Failed to get proxy configuration!", pc.getHost(), pc.getHost());
        assertEquals("Failed to get proxy configuration!", pc.getPort(), pc.getPort());
        assertEquals("Failed to get proxy configuration!", pc.getUsername(), pc.getUsername());
        assertEquals("Failed to get proxy configuration!", pc.getPassword(), pc.getPassword());
        assertEquals("Failed to get proxy configuration!", pc.getType(), pc.getType());
        */


        url = getContextBaseUrl() + "/configuration/strongbox/proxy-configuration";

        given().params("storageId", storageId, "repositoryId", repositoryId1)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .extract();

        Storage storage = getStorage(storageId);

        assertNotNull("Failed to get storage (" + storageId + ")!", storage);
        assertFalse("Failed to get storage (" + storageId + ")!", storage.getRepositories().isEmpty());

        //    response = client.deleteRepository(storageId, repositoryId1, true);

        url = getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId + "/" + repositoryId1;
        logger.debug(url);

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .param("force", true)
               .when()
               .delete(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(ResponseStatusEnum.OK.value()));

        url = getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId + "/" + repositoryId1;

        logger.debug(storageId);
        logger.debug(repositoryId1);

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .peek() // Use peek() to print the output
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(containsString("Repository " + storageId + ":" + repositoryId1 + " was not found."));
    }

    @Test
    public void testDeleteStorageWithNoExistingIdShouldReturnNotFoundStatus()
            throws Exception
    {
        String storageId = "storageNotFound";
        String url = getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId;

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .delete(url)
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(equalTo("Storage " + storageId + " not found."));
    }

    @Test
    public void testGetStorageWithNoExistingIdShouldReturnNotFoundStatus()
            throws Exception
    {
        String storageId = "storageNotFound";
        String url = getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId;

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(containsString("Storage " + storageId + " was not found."));
    }

    @Test
    public void testGetRepositoryWithNoExistingRepositoryIdShouldReturnNotFoundStatus()
            throws Exception
    {
        String storageId = "storage1";
        Storage storage1 = new Storage(storageId);

        String url = getContextBaseUrl() + "/configuration/strongbox/storages";

        logger.debug("Using storage class " + storage1.getClass()
                                                      .getName());

        given().contentType(MediaType.APPLICATION_XML_VALUE)
               .body(storage1)
               .when()
               .put(url)
               .prettyPeek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(ResponseStatusEnum.OK.value()));

        String repositoryId = "repositoryNotFound";
        url = getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId + "/" + repositoryId;

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(containsString("Repository " + storageId + ":" + repositoryId + " was not found."));
    }

    @Test
    public void testGetRepositoryWithNoExistingStorageIdShouldReturnNotFoundStatus()
            throws Exception
    {
        String storageId = "storageNotFound";
        String repositoryId = "repositoryNotFound";
        String url = getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId + "/" + repositoryId;

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(containsString("Repository " + storageId + ":" + repositoryId + " was not found."));
    }

    @Test
    public void testGetAndSetConfiguration()
            throws IOException, JAXBException
    {
        Configuration configuration = getConfigurationFromRemote();

        Storage storage = new Storage("storage3");

        configuration.addStorage(storage);

        String url = getContextBaseUrl() + "/configuration/strongbox/xml";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(configuration)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(ResponseStatusEnum.OK.value()));

        final Configuration c = getConfigurationFromRemote();

        assertNotNull("Failed to create storage3!", c.getStorage("storage3"));
    }

    public Configuration getConfigurationFromRemote()
    {
        String url = getContextBaseUrl() + "/configuration/strongbox/xml";

        return given().contentType(MediaType.TEXT_PLAIN_VALUE)
                      .when()
                      .get(url)
                      .as(Configuration.class);
    }

    @Test
    public void addAcceptedRuleSet()
            throws Exception
    {
        acceptedRuleSet();
    }

    @Test
    public void removeAcceptedRuleSet()
            throws Exception
    {
        acceptedRuleSet();

        String url = getContextBaseUrl() + "/configuration/strongbox/routing/rules/set/accepted/group-releases-2";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(ResponseStatusEnum.OK.value()));
    }

    @Test
    public void removeAcceptedRuleSetWithNoExistingGroupShouldReturnNotFoundStatus()
            throws Exception
    {
        acceptedRuleSet();

        String groupRepository = "groupRepositoryNotFound";
        String url = getContextBaseUrl() + "/configuration/strongbox/routing/rules/set/accepted/" + groupRepository;

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(url)
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(equalTo("Element was not found."));
    }

    @Test
    public void addAcceptedRepository()
            throws Exception
    {
        acceptedRuleSet();
        acceptedRepository();
    }

    @Test
    public void addAcceptedRepositoryWithoutReposShouldReturnBadRequestStatus()
            throws Exception
    {
        String url =
                getContextBaseUrl() + "/configuration/strongbox/routing/rules/accepted/group-releases-2/repositories";

        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(null);
        Set<String> repositories = new HashSet<>();
        routingRule.setRepositories(repositories);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(routingRule)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(equalTo("Routing rule is empty"));
    }

    @Test
    public void addAcceptedRepositoryWithNoExistingGroupShouldReturnNotFoundStatus()
            throws Exception
    {
        String groupRepository = "groupRepositoryNotFound";
        String url = getContextBaseUrl() + "/configuration/strongbox/routing/rules/accepted/" + groupRepository +
                     "/repositories";

        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(".*some.test");
        Set<String> repositories = new HashSet<>();
        repositories.add("releases2");
        repositories.add("releases3");
        routingRule.setRepositories(repositories);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(routingRule)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(equalTo("Element was not found."));
    }

    @Test
    public void removeAcceptedRepository()
            throws Exception
    {
        acceptedRuleSet();
        acceptedRepository();

        String url = getContextBaseUrl() +
                     "/configuration/strongbox/routing/rules/accepted/group-releases-2/repositories/releases3?pattern=.*some.test";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(ResponseStatusEnum.OK.value()));
    }

    @Test
    public void removeAcceptedRepositoryWithNoExistingGroupShouldReturnNotFoundStatus()
            throws Exception
    {
        acceptedRuleSet();
        acceptedRepository();

        String groupRepository = "groupRepositoryNotFound";
        String url = getContextBaseUrl() +
                     "/configuration/strongbox/routing/rules/accepted/" + groupRepository +
                     "/repositories/releases3?pattern=.*some.test";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(url)
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(equalTo("Element was not found."));
    }

    @Test
    public void overrideAcceptedRepository()
            throws Exception
    {
        acceptedRuleSet();
        acceptedRepository();

        String url = getContextBaseUrl() +
                     "/configuration/strongbox/routing/rules/accepted/group-releases-2/override/repositories";

        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(".*some.test");
        Set<String> repositories = new HashSet<String>();
        repositories.add("releases22");
        repositories.add("releases32");
        routingRule.setRepositories(repositories);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(routingRule)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(ResponseStatusEnum.OK.value()));
    }

    @Test
    public void overrideAcceptedRepositoryWithoutReposShouldReturnBadRequestStatus()
            throws Exception
    {
        acceptedRuleSet();
        acceptedRepository();

        String url = getContextBaseUrl() +
                     "/configuration/strongbox/routing/rules/accepted/group-releases-2/override/repositories";

        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(null);
        Set<String> repositories = new HashSet<>();
        routingRule.setRepositories(repositories);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(routingRule)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(equalTo("Routing rule is empty"));
    }

    @Test
    public void overrideAcceptedRepositoryWithoutGroupShouldReturnNotFoundStatus()
            throws Exception
    {
        acceptedRuleSet();
        acceptedRepository();

        String groupRepository = "groupRepositoryNotFound";
        String url = getContextBaseUrl() +
                     "/configuration/strongbox/routing/rules/accepted/" + groupRepository + "/override/repositories";

        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(".*some.test");
        Set<String> repositories = new HashSet<>();
        repositories.add("releases22");
        repositories.add("releases32");
        routingRule.setRepositories(repositories);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(routingRule)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.NOT_FOUND.value())
               .body(equalTo("Element was not found."));
    }

    private void acceptedRuleSet()
            throws IOException
    {
        String url = getContextBaseUrl() + "/configuration/strongbox/routing/rules/set/accepted";

        RuleSet ruleSet = new RuleSet();
        ruleSet.setGroupRepository("group-releases-2");
        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(".*some.test");
        Set<String> repositories = new HashSet<String>();
        repositories.add("releases-with-trash");
        repositories.add("releases-with-redeployment");
        routingRule.setRepositories(repositories);

        List<RoutingRule> rule = new LinkedList<>();
        rule.add(routingRule);
        ruleSet.setRoutingRules(rule);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(ruleSet)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(ResponseStatusEnum.OK.value()));
    }

    private void acceptedRepository()
            throws IOException
    {
        String url =
                getContextBaseUrl() + "/configuration/strongbox/routing/rules/accepted/group-releases-2/repositories";

        RoutingRule routingRule = new RoutingRule();
        routingRule.setPattern(".*some.test");
        Set<String> repositories = new HashSet<String>();
        repositories.add("releases2");
        repositories.add("releases3");
        routingRule.setRepositories(repositories);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .body(routingRule)
               .when()
               .put(url)
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(ResponseStatusEnum.OK.value()));
    }

    private ProxyConfiguration createProxyConfiguration()
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

}
