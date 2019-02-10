package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.forms.configuration.*;
import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.xml.configuration.repository.MavenRepositoryConfiguration;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.http.pool.PoolStats;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
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
import static org.carlspring.strongbox.controllers.configuration.StoragesConfigurationController.*;
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

    private RepositoryForm repositoryForm0;

    private RepositoryForm repositoryForm1;

    @Inject
    private PropertiesBooter propertiesBooter;

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

        repositoryForm0 = new RepositoryForm();
        repositoryForm0.setId("repository0");
        repositoryForm0.setAllowsRedeployment(true);
        repositoryForm0.setSecured(true);
        repositoryForm0.setLayout(Maven2LayoutProvider.ALIAS);
        repositoryForm0.setType("hosted");
        repositoryForm0.setPolicy("release");
        repositoryForm0.setImplementation("file-system");
        repositoryForm0.setStatus("In Service");

        repositoryForm1 = new RepositoryForm();
        repositoryForm1.setId("repository1");
        repositoryForm1.setAllowsForceDeletion(true);
        repositoryForm1.setTrashEnabled(true);
        repositoryForm1.setProxyConfiguration(createProxyConfiguration());
        repositoryForm1.setLayout(Maven2LayoutProvider.ALIAS);
        repositoryForm1.setType("hosted");
        repositoryForm1.setPolicy("release");
        repositoryForm1.setImplementation("file-system");
        repositoryForm1.setStatus("In Service");
        repositoryForm1.setGroupRepositories(ImmutableSet.of("repository0"));
    }

    private String getBaseDir(String storageId)
    {
        String directory = propertiesBooter.getVaultDirectory() + "/storages/" + storageId;

        return Paths.get(directory).toAbsolutePath().toString();
    }

    @Test
    public void testGetStorages()
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
    public void testCreateAndUpdateStorage()
    {
        final String storageId = VALID_STORAGE_ID;

        StorageForm storage1 = buildStorageForm(storageId);

        String url = getContextBaseUrl();

        logger.debug("Using storage class " + storage1.getClass().getName());

        // 1. Create storage
        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(storage1)
                     .when()
                     .put(url)
                     .prettyPeek()
                     .then()
                     .statusCode(OK)
                     .body(containsString(SUCCESSFUL_SAVE_STORAGE));

        Storage storage = getStorage(storageId);

        assertNotNull(storage, "Failed to get storage (" + storageId + ")!");
        assertEquals(storage.getBasedir(), storage1.getBasedir());

        // 2. Update storage.
        url = getContextBaseUrl() + "/" + storageId;
        String newBasedir = getBaseDir(storageId) + "-updated";
        storage1.setBasedir(newBasedir);

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(storage1)
                     .when()
                     .put(url)
                     .prettyPeek()
                     .then()
                     .statusCode(OK)
                     .body(containsString(SUCCESSFUL_UPDATE_STORAGE));

        storage = getStorage(storageId);

        assertNotNull(storage, "Failed to get storage (" + storageId + ")!");
        assertEquals(storage.getBasedir(), storage1.getBasedir(),
                     "Failed to update storage (" + storageId + ") basedir!");
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
    public void testAddGetRepository()
    {
        final String storageId = EXISTING_STORAGE_ID;

        StorageForm storage0 = buildStorageForm(storageId);

        RepositoryForm repositoryForm0_1 = new RepositoryForm();
        repositoryForm0_1.setId("repository0_1");
        repositoryForm0_1.setAllowsRedeployment(true);
        repositoryForm0_1.setSecured(true);
        repositoryForm0_1.setLayout(Maven2LayoutProvider.ALIAS);
        MavenRepositoryConfigurationForm mavenRepositoryConfigurationForm = new MavenRepositoryConfigurationForm();
        mavenRepositoryConfigurationForm.setIndexingEnabled(true);
        mavenRepositoryConfigurationForm.setIndexingClassNamesEnabled(false);
        repositoryForm0_1.setRepositoryConfiguration(mavenRepositoryConfigurationForm);
        repositoryForm0_1.setType("hosted");
        repositoryForm0_1.setPolicy("release");
        repositoryForm0_1.setImplementation("file-system");
        repositoryForm0_1.setStatus("In Service");
        Set<String> groupRepositories = new LinkedHashSet<>();
        String groupRepository1 = "maven-central";
        String groupRepository2 = "carlspring";
        groupRepositories.add(groupRepository1);
        groupRepositories.add(groupRepository2);
        repositoryForm0_1.setGroupRepositories(groupRepositories);

        Integer maxConnectionsRepository2 = 30;

        RepositoryForm repositoryForm0_2 = new RepositoryForm();
        repositoryForm0_2.setId("repository0_2");
        repositoryForm0_2.setAllowsForceDeletion(true);
        repositoryForm0_2.setTrashEnabled(true);
        repositoryForm0_2.setProxyConfiguration(createProxyConfiguration());
        repositoryForm0_2.setLayout(Maven2LayoutProvider.ALIAS);
        repositoryForm0_2.setType("proxy");
        repositoryForm0_2.setPolicy("release");
        repositoryForm0_2.setImplementation("file-system");
        repositoryForm0_2.setStatus("In Service");
        repositoryForm0_2.setGroupRepositories(ImmutableSet.of("repository0"));
        repositoryForm0_2.setHttpConnectionPool(maxConnectionsRepository2);

        String secondRepositoryUrl = "http://abc.def";

        RemoteRepositoryForm remoteRepositoryForm = new RemoteRepositoryForm();
        remoteRepositoryForm.setUrl(secondRepositoryUrl);
        remoteRepositoryForm.setCheckIntervalSeconds(1000);
        repositoryForm0_2.setRemoteRepository(remoteRepositoryForm);

        addRepository(repositoryForm0_1, storage0);
        addRepository(repositoryForm0_2, storage0);

        Storage storage = getStorage(storageId);
        Repository repository0 = storage.getRepositories().get(repositoryForm0_1.getId());
        Repository repository1 = storage.getRepositories().get(repositoryForm0_2.getId());

        Map<String, String> groupRepositoriesMap = repository0.getGroupRepositories();
        Map<String, String> groupRepositoriesMapExpected = new LinkedHashMap<>();
        groupRepositoriesMapExpected.put(groupRepository1, groupRepository1);
        groupRepositoriesMapExpected.put(groupRepository2, groupRepository2);

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
        assertEquals(groupRepositoriesMapExpected, groupRepositoriesMap);

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

        deleteRepository(storage0.getId(), repositoryForm0_1.getId());
        deleteRepository(storage0.getId(), repositoryForm0_2.getId());
    }

    @Test
    public void testUpdatingRepositoryWithNonExistingStorage()
    {
        String url = getContextBaseUrl() + "/non-existing-storage/fake-repository";
        RepositoryForm form = new RepositoryForm();

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(form)
                     .when()
                     .put(url)
                     .peek()
                     .then()
                     .statusCode(404);
    }

    private Storage getStorage(String storageId)
    {
        String url = getContextBaseUrl() + "/" + storageId;

        return givenCustom().accept(MediaType.APPLICATION_JSON_VALUE)
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
                         .statusCode(OK)
                         .body(containsString(SUCCESSFUL_REPOSITORY_SAVE));

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
        String url = String.format("%s/%s/%s?force=%s", getContextBaseUrl(), storageId, repositoryId, true);

        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .when()
                     .delete(url)
                     .then()
                     .statusCode(OK)
                     .body(containsString(SUCCESSFUL_REPOSITORY_REMOVAL));

        String repoDir = getBaseDir(storageId) + "/" + repositoryId;

        MatcherAssert.assertThat(Files.exists(Paths.get(repoDir)), CoreMatchers.equalTo(false));
    }

    @Test
    public void testCreateAndDeleteStorage()
    {
        final String storageId = "storage2";
        final String repositoryId0 = repositoryForm0.getId();
        final String repositoryId1 = repositoryForm1.getId();

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
                     .statusCode(OK)
                     .body(containsString(SUCCESSFUL_SAVE_STORAGE));

        // 1.1. Add repositories to storage.
        addRepository(repositoryForm0, storage2);
        addRepository(repositoryForm1, storage2);

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

        url = getContextBaseUrl() + "/" + storageId;

        logger.debug(url);

        // 3. Delete storage created.
        givenCustom().contentType(MediaType.TEXT_PLAIN_VALUE)
                     .accept(MediaType.TEXT_PLAIN_VALUE)
                     .param("force", true)
                     .when()
                     .delete(url)
                     .peek() // Use peek() to print the output
                     .then()
                     .statusCode(OK)
                     .body(containsString(SUCCESSFUL_STORAGE_REMOVAL));

        url = getContextBaseUrl() + "/" + storageId;

        logger.debug(storageId);
        logger.debug(repositoryId0);

        // 4. Check that the storage deleted does not exist anymore.
        givenCustom().contentType(MediaType.TEXT_PLAIN_VALUE)
                     .when()
                     .get(url)
                     .peek() // Use peek() to print the output
                     .then()
                     .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void whenStorageIsCreatedWithoutBasedirProvidedDefaultIsSet()
    {
        final String storageId = "storage3";

        StorageForm storage3 = buildStorageForm(storageId);
        storage3.setBasedir(null);

        String url = getContextBaseUrl();

        // 1. Create storage without base dir provided.
        givenCustom().contentType(MediaType.APPLICATION_JSON_VALUE)
                     .accept(MediaType.APPLICATION_JSON_VALUE)
                     .body(storage3)
                     .when()
                     .put(url)
                     .peek() // Use peek() to print the output
                     .then()
                     .statusCode(OK)
                     .body(containsString(SUCCESSFUL_SAVE_STORAGE));

        Storage storage = getStorage(storageId);
        assertNotNull(storage, "Failed to get storage (" + storageId + ")!");

        // 2. Confirm default base dir has been created
        String storageBaseDir = getBaseDir(storageId);
        MatcherAssert.assertThat(Files.exists(Paths.get(storageBaseDir)), CoreMatchers.equalTo(true));

        url = getContextBaseUrl() + "/" + storageId;

        // 3. Delete storage created.
        givenCustom().contentType(MediaType.TEXT_PLAIN_VALUE)
                     .accept(MediaType.TEXT_PLAIN_VALUE)
                     .param("force", true)
                     .when()
                     .delete(url)
                     .peek() // Use peek() to print the output
                     .then()
                     .statusCode(OK)
                     .body(containsString(SUCCESSFUL_STORAGE_REMOVAL));

        // 4. Check that the storage deleted does not exist anymore.
        givenCustom().contentType(MediaType.TEXT_PLAIN_VALUE)
                     .when()
                     .get(url)
                     .peek() // Use peek() to print the output
                     .then()
                     .statusCode(HttpStatus.NOT_FOUND.value());

        // 5. Confirm base dir has been deleted
        MatcherAssert.assertThat(Files.exists(Paths.get(storageBaseDir)), CoreMatchers.equalTo(false));
    }

}
