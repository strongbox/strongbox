package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.MutableRepository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpServerErrorException;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static org.carlspring.strongbox.controllers.configuration.ProxyConfigurationControllerTestIT.createProxyConfiguration;
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

    @Test
    public void testAddGetStorage()
    {
        String storageId = "storage1";

        MutableStorage storage1 = new MutableStorage("storage1");

        String url = getContextBaseUrl() + "/api/configuration/strongbox/storages";

        logger.debug("Using storage class " + storage1.getClass()
                                                      .getName());

        given().contentType(MediaType.APPLICATION_XML_VALUE)
               .body(storage1)
               .when()
               .put(url)
               .prettyPeek()
               .then()
               .statusCode(200);

        MutableRepository r1 = new MutableRepository("repository0");
        r1.setAllowsRedeployment(true);
        r1.setSecured(true);
        r1.setStorage(storage1);
        r1.setLayout(Maven2LayoutProvider.ALIAS);

        MutableRepository r2 = new MutableRepository("repository1");
        r2.setAllowsForceDeletion(true);
        r2.setTrashEnabled(true);
        r2.setStorage(storage1);
        r2.setProxyConfiguration(createProxyConfiguration());
        r2.setLayout(Maven2LayoutProvider.ALIAS);

        addRepository(r1);
        addRepository(r2);

        MutableStorage storage = getStorage(storageId);

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

        deleteRepository(storageId, "repository0");
        deleteRepository(storageId, "repository1");
    }

    private MutableStorage getStorage(String storageId)
    {
        String url = getContextBaseUrl() + "/api/configuration/strongbox/storages/" + storageId;

        return given().contentType(MediaType.TEXT_PLAIN_VALUE)
                      .accept(MediaType.APPLICATION_XML_VALUE)
                      .when()
                      .get(url)
                      .as(MutableStorage.class);
    }

    private int addRepository(MutableRepository repository)
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
            url = getContextBaseUrl() + "/api/configuration/strongbox/storages/" + repository.getStorage().getId() +
                  "/" +
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

        MutableStorage storage2 = new MutableStorage(storageId);

        String url = getContextBaseUrl() + "/api/configuration/strongbox/storages";

        given().contentType(MediaType.APPLICATION_XML_VALUE)
               .body(storage2)
               .when()
               .put(url)
               .peek() // Use peek() to print the ouput
               .then()
               .statusCode(200);

        MutableRepository r1 = new MutableRepository(repositoryId1);
        r1.setAllowsRedeployment(true);
        r1.setSecured(true);
        r1.setStorage(storage2);
        r1.setProxyConfiguration(createProxyConfiguration());
        r1.setLayout(Maven2LayoutProvider.ALIAS);

        MutableRepository r2 = new MutableRepository(repositoryId2);
        r2.setAllowsRedeployment(true);
        r2.setSecured(true);
        r2.setStorage(storage2);
        r2.setLayout(Maven2LayoutProvider.ALIAS);

        addRepository(r1);
        addRepository(r2);

        url = getContextBaseUrl() + "/api/configuration/strongbox/proxy-configuration";

        given().accept(MediaType.APPLICATION_JSON_VALUE)
               .params("storageId", storageId, "repositoryId", repositoryId1)
               .when()
               .get(url)
               .peek() // Use peek() to print the ouput
               .then()
               .statusCode(200)
               .extract();

        MutableStorage storage = getStorage(storageId);

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
