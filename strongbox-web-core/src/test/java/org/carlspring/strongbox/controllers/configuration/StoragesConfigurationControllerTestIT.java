package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.configuration.*;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import javax.inject.Inject;
import java.nio.file.Paths;
import java.util.List;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.http.pool.PoolStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpServerErrorException;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.controllers.configuration.StoragesConfigurationController.FAILED_SAVE_STORAGE_FORM_ERROR;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class StoragesConfigurationControllerTestIT
        extends RestAssuredBaseTest
{

    private static final String VALID_STORAGE_ID = "storage1";
    private static final String EXISTING_STORAGE_ID = STORAGE0;
    private RepositoryForm r0;
    private RepositoryForm r1;

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

    @Override
    @BeforeEach
    public void init()
            throws Exception
    {
        super.init();
        setContextBaseUrl("/api/configuration/strongbox/storages");

        r0 = new RepositoryForm();
        r0.setId("repository0");
        r0.setAllowsRedeployment(true);
        r0.setSecured(true);
        r0.setLayout(Maven2LayoutProvider.ALIAS);
        r0.setType("hosted");
        r0.setPolicy("release");
        r0.setImplementation("file-system");
        r0.setStatus("In Service");

        r1 = new RepositoryForm();
        r1.setId("repository1");
        r1.setAllowsForceDeletion(true);
        r1.setTrashEnabled(true);
        r1.setProxyConfiguration(createProxyConfiguration());
        r1.setLayout(Maven2LayoutProvider.ALIAS);
        r1.setType("hosted");
        r1.setPolicy("release");
        r1.setImplementation("file-system");
        r1.setStatus("In Service");
        r1.setGroupRepositories(ImmutableSet.of("repository0"));
    }

    private String getBaseDir(String storageId)
    {
        String directory = ConfigurationResourceResolver.getVaultDirectory() + "/storages/" + storageId;
        return Paths.get(directory).toAbsolutePath().toString();
    }

    @Test
    public void testGetStorages()
            throws Exception
    {
        String url = getContextBaseUrl();

        givenCustom().accept(MediaType.APPLICATION_JSON_VALUE)
                     .when()
                     .get(url)
                     .peek()
                     .then()
                     .statusCode(OK);
    }

    @Test
    public void testGetStorage()
            throws Exception
    {
        String url = getContextBaseUrl() + "/" + EXISTING_STORAGE_ID;

        givenCustom().accept(MediaType.APPLICATION_JSON_VALUE)
                     .when()
                     .get(url)
                     .peek()
                     .then()
                     .statusCode(OK);
    }

    @Test
    public void testGetGroupRepository()
            throws Exception
    {
        String url = getContextBaseUrl() + "/storage-common-proxies/group-common-proxies";

        givenCustom().accept(MediaType.APPLICATION_JSON_VALUE)
                     .when()
                     .get(url)
                     .peek()
                     .then()
                     .statusCode(OK);
    }

    @Test
    public void testGetMavenRepository()
            throws Exception
    {
        String url = getContextBaseUrl() + "/" + EXISTING_STORAGE_ID + "/releases";

        givenCustom().accept(MediaType.APPLICATION_JSON_VALUE)
                     .when()
                     .get(url)
                     .peek()
                     .then()
                     .statusCode(OK);
    }

    @Test
    public void testCreateAndGetStorage()
    {
        final String storageId = VALID_STORAGE_ID;
        final String repositoryId0 = r0.getId();
        final String repositoryId1 = r1.getId();

        StorageForm storage1 = buildStorageForm(storageId);

        String url = getContextBaseUrl();

        logger.debug("Using storage class " + storage1.getClass()
                                                      .getName());

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(storage1)
                     .when()
                     .put(url)
                     .prettyPeek()
                     .then()
                     .statusCode(OK);

        addRepository(r0, storage1);
        addRepository(r1, storage1);

        Storage storage = getStorage(storageId);
        Repository repository0 = storage.getRepositories().get(repositoryId0);
        Repository repository1 = storage.getRepositories().get(repositoryId1);

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

        deleteRepository(storageId, repositoryId0);
        deleteRepository(storageId, repositoryId1);
    }

    @ParameterizedTest
    @ValueSource(strings = { MediaType.APPLICATION_JSON_VALUE,
                             MediaType.TEXT_PLAIN_VALUE })
    public void testCreatingStorageWithExistingIdShouldFail(String acceptHeader)
    {
        StorageForm form = buildStorageForm(EXISTING_STORAGE_ID);

        String url = getContextBaseUrl();

        given().contentType(MediaType.APPLICATION_JSON_VALUE)
               .accept(acceptHeader)
               .body(form)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.BAD_REQUEST.value())
               .body(containsString(FAILED_SAVE_STORAGE_FORM_ERROR));
    }

    private StorageForm buildStorageForm(final String storageId)
    {
        final String basedir = getBaseDir(storageId);

        StorageForm form = new StorageForm();
        form.setId(storageId);
        form.setBasedir(basedir);

        return form;
    }

    @Test
    public void testUpdateStorage()
    {
        final String storageId = EXISTING_STORAGE_ID;
        final String repositoryId0 = r0.getId();
        final String repositoryId1 = r1.getId();

        StorageForm storage0 = buildStorageForm(storageId);

        String url = getContextBaseUrl() + "/" + storageId;

        logger.debug("Using storage class " + storage0.getClass()
                                                      .getName());

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(storage0)
                     .when()
                     .put(url)
                     .prettyPeek()
                     .then()
                     .statusCode(OK);

        addRepository(r0, storage0);
        addRepository(r1, storage0);

        Storage storage = getStorage(storageId);
        Repository repository0 = storage.getRepositories().get(repositoryId0);
        Repository repository1 = storage.getRepositories().get(repositoryId1);

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

        deleteRepository(storageId, repositoryId0);
        deleteRepository(storageId, repositoryId1);
    }

    @Test
    public void testAddGetRepository()
    {
        final String storageId = EXISTING_STORAGE_ID;

        StorageForm storage0 = buildStorageForm(storageId);

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
        String url = getContextBaseUrl() + "/" + storageId;

        return givenCustom()
                       .accept(MediaType.APPLICATION_JSON_VALUE)
                       .when()
                       .get(url)
                       .prettyPeek()
                       .as(Storage.class);
    }

    private void addRepository(RepositoryForm repository,
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
            url = getContextBaseUrl() + "/" + storage.getId() + "/" + repository.getId();


            givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                         .accept(MediaType.APPLICATION_JSON_VALUE)
                         .body(repository)
                         .when()
                         .put(url)
                         .then()
                         .statusCode(OK);

        }
        catch (RuntimeException e)
        {
            logger.error("Unable to create web resource.", e);

            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void deleteRepository(String storageId,
                                  String repositoryId)
    {
        String url = getContextBaseUrl() + "/" + storageId + "/" + repositoryId;

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .when()
                     .delete(url)
                     .then()
                     .statusCode(OK);
    }

    @Test
    public void testCreateAndDeleteStorage()
    {
        final String storageId = "storage2";
        final String repositoryId0 = r0.getId();
        final String repositoryId1 = r1.getId();

        StorageForm storage2 = buildStorageForm(storageId);

        String url = getContextBaseUrl();

        // 1. Create storage.
        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(storage2)
                     .when()
                     .put(url)
                     .peek() // Use peek() to print the output
                     .then()
                     .statusCode(OK);

        // 1.1. Add repositories to storage.
        addRepository(r0, storage2);
        addRepository(r1, storage2);

        url = "/api/configuration/strongbox/proxy-configuration";

        // 2. Check proxy configuration from storage created.
        givenCustom().accept(MediaType.APPLICATION_JSON_VALUE)
                     .params("storageId", storageId, "repositoryId", repositoryId1)
                     .when()
                     .get(url)
                     .peek() // Use peek() to print the output
                     .then()
                     .statusCode(OK);

        Storage storage = getStorage(storageId);

        assertNotNull(storage, "Failed to get storage (" + storageId + ")!");
        assertFalse(storage.getRepositories().isEmpty(), "Failed to get storage (" + storageId + ")!");

        url = getContextBaseUrl() + "/" + storageId + "/" + repositoryId0;

        logger.debug(url);

        // 3. Delete storage created.
        givenCustom().contentType(MediaType.TEXT_PLAIN_VALUE)
                     .accept(MediaType.TEXT_PLAIN_VALUE)
                     .param("force", true)
                     .when()
                     .delete(url)
                     .peek() // Use peek() to print the output
                     .then()
                     .statusCode(OK);

        url = getContextBaseUrl() + "/" + storageId + "/" + repositoryId0;

        logger.debug(storageId);
        logger.debug(repositoryId0);

        // 4. Check that the storage deleted does not exist anymore.
        givenCustom().contentType(MediaType.TEXT_PLAIN_VALUE)
                     .when()
                     .get(url)
                     .peek() // Use peek() to print the output
                     .then()
                     .statusCode(HttpStatus.NOT_FOUND.value());

        // 5. Delete repositories from deleted storage.
        deleteRepository(storageId, repositoryId1);
    }
}
