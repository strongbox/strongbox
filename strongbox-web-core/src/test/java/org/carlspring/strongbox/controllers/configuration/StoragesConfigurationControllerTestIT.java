package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.configuration.*;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import javax.inject.Inject;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;
import org.apache.http.pool.PoolStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpServerErrorException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class StoragesConfigurationControllerTestIT
        extends RestAssuredBaseTest
{

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

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

        givenCustom().accept(MediaType.APPLICATION_JSON_VALUE)
                     .when()
                     .get(url)
                     .peek()
                     .then()
                     .statusCode(200);
    }

    @Test
    public void testGetStorage()
            throws Exception
    {
        String url = getContextBaseUrl() + "/api/configuration/strongbox/storages/storage0";

        givenCustom().accept(MediaType.APPLICATION_JSON_VALUE)
                     .when()
                     .get(url)
                     .peek()
                     .then()
                     .statusCode(200);
    }

    @Test
    public void testGetGroupRepository()
            throws Exception
    {
        String url = getContextBaseUrl() +
                     "/api/configuration/strongbox/storages/storage-common-proxies/group-common-proxies";

        givenCustom().accept(MediaType.APPLICATION_JSON_VALUE)
                     .when()
                     .get(url)
                     .peek()
                     .then()
                     .statusCode(200);
    }

    @Test
    public void testGetMavenRepository()
            throws Exception
    {
        String url = getContextBaseUrl() +
                     "/api/configuration/strongbox/storages/storage0/releases";

        givenCustom().accept(MediaType.APPLICATION_JSON_VALUE)
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

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
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
        r2.setGroupRepositories(ImmutableSet.of("repository0"));

        addRepository(r1, storage1);
        addRepository(r2, storage1);

        Storage storage = getStorage(storageId);
        Repository repository0 = storage.getRepositories().get("repository0");
        Repository repository1 = storage.getRepositories().get("repository1");

        assertNotNull(storage, "Failed to get storage (" + storageId + ")!");
        assertFalse(storage.getRepositories().isEmpty(), "Failed to get storage (" + storageId + ")!");
        assertTrue(repository0.allowsRedeployment(),
                   "Failed to get storage (" + storageId + ")!");
        assertTrue(repository0.isSecured(),
                   "Failed to get storage (" + storageId + ")!");
        assertTrue(repository1.allowsForceDeletion(),
                   "Failed to get storage (" + storageId + ")!");
        assertTrue(repository1.isTrashEnabled(),
                   "Failed to get storage (" + storageId + ")!");


        assertNotNull(repository1.getProxyConfiguration().getHost(),
                      "Failed to get storage (" + storageId + ")!");
        assertEquals("localhost",
                     repository1.getProxyConfiguration().getHost(),
                     "Failed to get storage (" + storageId + ")!");

        deleteRepository(storageId, "repository0");
        deleteRepository(storageId, "repository1");
    }

    @Test
    public void testAddGetRepository()
    {
        StorageForm storage0 = new StorageForm();
        String storageId = "storage0";
        storage0.setId(storageId);

        RepositoryForm r0_1 = new RepositoryForm();
        r0_1.setId("repository0_1");
        r0_1.setAllowsRedeployment(true);
        r0_1.setSecured(true);
        r0_1.setLayout(Maven2LayoutProvider.ALIAS);
        MavenRepositoryConfigurationForm mavenRepositoryConfigurationForm = new MavenRepositoryConfigurationForm();
        mavenRepositoryConfigurationForm.setIndexingEnabled(true);
        mavenRepositoryConfigurationForm.setIndexingClassNamesEnabled(false);
        r0_1.setRepositoryConfiguration(mavenRepositoryConfigurationForm);
        r0_1.setType("hosted");
        r0_1.setPolicy("release");
        r0_1.setImplementation("file-system");
        r0_1.setStatus("In Service");

        Integer maxConnectionsRepository2 = 30;

        RepositoryForm r0_2 = new RepositoryForm();
        r0_2.setId("repository0_2");
        r0_2.setAllowsForceDeletion(true);
        r0_2.setTrashEnabled(true);
        r0_2.setProxyConfiguration(createProxyConfiguration());
        r0_2.setLayout(Maven2LayoutProvider.ALIAS);
        r0_2.setType("proxy");
        r0_2.setPolicy("release");
        r0_2.setImplementation("file-system");
        r0_2.setStatus("In Service");
        r0_2.setGroupRepositories(ImmutableSet.of("repository0"));
        r0_2.setHttpConnectionPool(maxConnectionsRepository2);

        String secondRepositoryUrl = "http://abc.def";

        RemoteRepositoryForm remoteRepositoryForm = new RemoteRepositoryForm();
        remoteRepositoryForm.setUrl(secondRepositoryUrl);
        remoteRepositoryForm.setCheckIntervalSeconds(1000);
        r0_2.setRemoteRepository(remoteRepositoryForm);

        addRepository(r0_1, storage0);
        addRepository(r0_2, storage0);

        Storage storage = getStorage(storageId);
        Repository repository0 = storage.getRepositories().get(r0_1.getId());
        Repository repository1 = storage.getRepositories().get(r0_2.getId());

        assertNotNull(storage, "Failed to get storage (" + storageId + ")!");
        assertFalse(storage.getRepositories().isEmpty(), "Failed to get storage (" + storageId + ")!");
        assertTrue(repository0.allowsRedeployment(),
                   "Failed to get storage (" + storageId + ")!");
        assertTrue(repository0.isSecured(),
                   "Failed to get storage (" + storageId + ")!");
        assertNotNull(repository0.getRepositoryConfiguration(),
                      "Failed to get storage (" + storageId + ")!");
        assertTrue(repository0.getRepositoryConfiguration() instanceof MavenRepositoryConfiguration,
                   "Failed to get storage (" + storageId + ")!");
        assertTrue(((MavenRepositoryConfiguration) repository0.getRepositoryConfiguration()).isIndexingEnabled(),
                   "Failed to get storage (" + storageId + ")!");
        assertFalse(
                ((MavenRepositoryConfiguration) repository0.getRepositoryConfiguration()).isIndexingClassNamesEnabled(),
                "Failed to get storage (" + storageId + ")!");

        assertTrue(repository1.allowsForceDeletion(),
                   "Failed to get storage (" + storageId + ")!");
        assertTrue(repository1.isTrashEnabled(),
                   "Failed to get storage (" + storageId + ")!");
        assertNotNull(repository1.getProxyConfiguration().getHost(),
                      "Failed to get storage (" + storageId + ")!");
        assertEquals("localhost",
                     repository1.getProxyConfiguration().getHost(),
                     "Failed to get storage (" + storageId + ")!");

        PoolStats poolStatsRepository2 = proxyRepositoryConnectionPoolConfigurationService.getPoolStats(
                secondRepositoryUrl);

        assertEquals(maxConnectionsRepository2.intValue(),
                     poolStatsRepository2.getMax(),
                     "Max connections for proxy repository not set accordingly!");

        deleteRepository(storage0.getId(), r0_1.getId());
        deleteRepository(storage0.getId(), r0_2.getId());
    }

    private Storage getStorage(String storageId)
    {
        String url = getContextBaseUrl() + "/api/configuration/strongbox/storages/" + storageId;

        RestAssuredMockMvcConfig config = RestAssuredMockMvcConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory(
                        (aClass, s) -> objectMapper
                ));

        return givenCustom()
                       .accept(MediaType.APPLICATION_JSON_VALUE)
                       .when()
                       .get(url)
                       .prettyPeek()
                       .as(Storage.class);
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

        int status = givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
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

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
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

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
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

        givenCustom().accept(MediaType.APPLICATION_JSON_VALUE)
                     .params("storageId", storageId, "repositoryId", repositoryId1)
                     .when()
                     .get(url)
                     .peek() // Use peek() to print the ouput
                     .then()
                     .statusCode(200)
                     .extract();

        Storage storage = getStorage(storageId);

        assertNotNull(storage, "Failed to get storage (" + storageId + ")!");
        assertFalse(storage.getRepositories().isEmpty(), "Failed to get storage (" + storageId + ")!");

        url = getContextBaseUrl() + "/api/configuration/strongbox/storages/" + storageId + "/" + repositoryId1;

        logger.debug(url);

        givenCustom().contentType(MediaType.TEXT_PLAIN_VALUE)
                     .accept(MediaType.TEXT_PLAIN_VALUE)
                     .param("force", true)
                     .when()
                     .delete(url)
                     .peek() // Use peek() to print the ouput
                     .then()
                     .statusCode(200);

        url = getContextBaseUrl() + "/api/configuration/strongbox/storages/" + storageId + "/" + repositoryId1;

        logger.debug(storageId);
        logger.debug(repositoryId1);

        givenCustom().contentType(MediaType.TEXT_PLAIN_VALUE)
                     .when()
                     .get(url)
                     .peek() // Use peek() to print the ouput
                     .then()
                     .statusCode(404);

        deleteRepository(storageId, repositoryId2);
    }
}
