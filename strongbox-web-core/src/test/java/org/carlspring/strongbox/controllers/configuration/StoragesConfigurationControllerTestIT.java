package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.configuration.ProxyConfigurationForm;
import org.carlspring.strongbox.forms.configuration.RepositoryForm;
import org.carlspring.strongbox.forms.configuration.StorageForm;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;

import java.util.List;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpServerErrorException;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@RunWith(SpringRunner.class)
public class StoragesConfigurationControllerTestIT
        extends RestAssuredBaseTest
{
    static ProxyConfigurationForm createProxyConfiguration()
    {
        ProxyConfigurationForm proxyConfiguration = new ProxyConfigurationForm();
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

    @Test
    public void testGetStorages()
            throws Exception
    {
        String url = getContextBaseUrl() + "/api/configuration/strongbox/storages";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(200);
    }

    @Test
    public void testAddGetStorage()
    {
        String storageId = "storage1";

        StorageForm storage1 = new StorageForm();
        storage1.setId("storage1");

        String url = getContextBaseUrl() + "/api/configuration/strongbox/storages";

        logger.debug("Using storage class " + storage1.getClass()
                                                      .getName());

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(storage1)
               .when()
               .put(url)
               .prettyPeek()
               .then()
               .statusCode(200);

        RepositoryForm r1 = new RepositoryForm();
        r1.setId("repository0");
        r1.setAllowsRedeployment(true);
        r1.setSecured(true);
        r1.setLayout(Maven2LayoutProvider.ALIAS);
        r1.setType("hosted");
        r1.setPolicy("release");
        r1.setImplementation("file-system");
        r1.setStatus("In Service");

        RepositoryForm r2 = new RepositoryForm();
        r2.setId("repository1");
        r2.setAllowsForceDeletion(true);
        r2.setTrashEnabled(true);
        r2.setProxyConfiguration(createProxyConfiguration());
        r2.setLayout(Maven2LayoutProvider.ALIAS);
        r2.setType("hosted");
        r2.setPolicy("release");
        r2.setImplementation("file-system");
        r2.setStatus("In Service");

        addRepository(r1, storage1);
        addRepository(r2, storage1);

        StorageOutput storage = getStorage(storageId);
        RepositoryOutput repository0 = storage.getRepositories().stream().filter(r->"repository0".equals(r.getId())).findFirst().get();
        RepositoryOutput repository1 = storage.getRepositories().stream().filter(r->"repository1".equals(r.getId())).findFirst().get();

        assertNotNull("Failed to get storage (" + storageId + ")!", storage);
        assertFalse("Failed to get storage (" + storageId + ")!", storage.getRepositories().isEmpty());
        assertTrue("Failed to get storage (" + storageId + ")!",
                   repository0.isAllowsRedeployment());
        assertTrue("Failed to get storage (" + storageId + ")!",
                   repository0.isSecured());
        assertTrue("Failed to get storage (" + storageId + ")!",
                   repository1.isAllowsForceDeletion());
        assertTrue("Failed to get storage (" + storageId + ")!",
                   repository1.isTrashEnabled());

        /* TODO
        assertNotNull("Failed to get storage (" + storageId + ")!",
                      repository1.getProxyConfiguration().getHost());
        assertEquals("Failed to get storage (" + storageId + ")!",
                     "localhost",
                     repository1.getProxyConfiguration().getHost());
                     */

        deleteRepository(storageId, "repository0");
        deleteRepository(storageId, "repository1");
    }

    private StorageOutput getStorage(String storageId)
    {
        String url = getContextBaseUrl() + "/api/configuration/strongbox/storages/" + storageId;

        return given().accept(MediaType.APPLICATION_JSON_VALUE)
                      .when()
                      .get(url)
                      .as(StorageOutput.class);
    }

    private int addRepository(RepositoryForm repository,
                              final StorageForm storage)
    {
        String url;
        if (repository == null)
        {
            logger.error("Unable to add non-existing repository.");

            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                               "Unable to add non-existing repository.");
        }

        if (storage == null)
        {
            logger.error("Storage associated with repo is null.");

            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR,
                                               "Storage associated with repo is null.");
        }

        try
        {
            url = getContextBaseUrl() + "/api/configuration/strongbox/storages/" + storage.getId() +
                  "/" +
                  repository.getId();
        }
        catch (RuntimeException e)
        {
            logger.error("Unable to create web resource.", e);

            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        int status = given().contentType(MediaType.APPLICATION_JSON_VALUE)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .body(repository)
                            .when()
                            .put(url)
                            .then()
                            .statusCode(200)
                            .extract()
                            .statusCode();

        return status;
    }

    private void deleteRepository(String storageId,
                                  String repositoryId)
    {
        String url = getContextBaseUrl() + String.format("/api/configuration/strongbox/storages/%s/%s",
                                                         storageId,
                                                         repositoryId);

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .when()
               .delete(url)
               .then()
               .statusCode(200);
    }

    @Test
    public void testCreateAndDeleteStorage()
    {
        final String storageId = "storage2";
        final String repositoryId1 = "repository0";
        final String repositoryId2 = "repository1";

        StorageForm storage2 = new StorageForm();
        storage2.setId(storageId);

        String url = getContextBaseUrl() + "/api/configuration/strongbox/storages";

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(MediaType.APPLICATION_JSON_VALUE)
               .body(storage2)
               .when()
               .put(url)
               .peek() // Use peek() to print the ouput
               .then()
               .statusCode(200);

        RepositoryForm r1 = new RepositoryForm();
        r1.setId(repositoryId1);
        r1.setAllowsRedeployment(true);
        r1.setSecured(true);
        r1.setProxyConfiguration(createProxyConfiguration());
        r1.setLayout(Maven2LayoutProvider.ALIAS);
        r1.setType("hosted");
        r1.setPolicy("release");
        r1.setImplementation("file-system");
        r1.setStatus("In Service");

        RepositoryForm r2 = new RepositoryForm();
        r2.setId(repositoryId2);
        r2.setAllowsRedeployment(true);
        r2.setSecured(true);
        r2.setLayout(Maven2LayoutProvider.ALIAS);
        r2.setType("hosted");
        r2.setPolicy("release");
        r2.setImplementation("file-system");
        r2.setStatus("In Service");

        addRepository(r1, storage2);
        addRepository(r2, storage2);

        url = getContextBaseUrl() + "/api/configuration/strongbox/proxy-configuration";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .params("storageId", storageId, "repositoryId", repositoryId1)
               .when()
               .get(url)
               .peek() // Use peek() to print the ouput
               .then()
               .statusCode(200)
               .extract();

        StorageOutput storage = getStorage(storageId);

        assertNotNull("Failed to get storage (" + storageId + ")!", storage);
        assertFalse("Failed to get storage (" + storageId + ")!", storage.getRepositories().isEmpty());

        url = getContextBaseUrl() + "/api/configuration/strongbox/storages/" + storageId + "/" + repositoryId1;

        logger.debug(url);

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .param("force", true)
               .when()
               .delete(url)
               .peek() // Use peek() to print the ouput
               .then()
               .statusCode(200);

        url = getContextBaseUrl() + "/api/configuration/strongbox/storages/" + storageId + "/" + repositoryId1;

        logger.debug(storageId);
        logger.debug(repositoryId1);

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .peek() // Use peek() to print the ouput
               .then()
               .statusCode(404);

        deleteRepository(storageId, repositoryId2);
    }
}
