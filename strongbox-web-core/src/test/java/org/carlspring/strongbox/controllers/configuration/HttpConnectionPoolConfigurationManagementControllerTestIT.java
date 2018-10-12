package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.config.IntegrationTest;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.collections.MapUtils;
import org.apache.http.pool.PoolStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author Pablo Tirado
 */
@IntegrationTest
@ExtendWith(SpringExtension.class)
public class HttpConnectionPoolConfigurationManagementControllerTestIT
        extends RestAssuredBaseTest
{

    @Inject
    private ConfigurationManager configurationManager;

    @BeforeEach
    public void setUp()
            throws IOException
    {
        Path storageBasedir = Paths.get(ConfigurationResourceResolver.getVaultDirectory(), "storages", "storage0",
                                        "org", "carlspring", "strongbox", "strongbox-utils", "8.2",
                                        "strongbox-utils-8.2.jar");

        generateArtifact(storageBasedir.toAbsolutePath().toString());
    }

    @Test
    public void testSetAndGetMaxNumberOfConnectionsForProxyRepositoryWithTextAcceptHeader()
    {
        int newMaxNumberOfConnections = 200;

        String url = getContextBaseUrl() + "/api/configuration/proxy/connection-pool/max/" + newMaxNumberOfConnections;

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo("Max number of connections for proxy repository was updated successfully."));

        url = getContextBaseUrl() + "/api/configuration/proxy/connection-pool";

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(String.valueOf(newMaxNumberOfConnections)));
    }

    @Test
    public void testSetAndGetMaxNumberOfConnectionsForProxyRepositoryWithJsonAcceptHeader()
    {
        int newMaxNumberOfConnections = 200;

        String url = getContextBaseUrl() + "/api/configuration/proxy/connection-pool/max/" + newMaxNumberOfConnections;

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("Max number of connections for proxy repository was updated successfully."));

        url = getContextBaseUrl() + "/api/configuration/proxy/connection-pool";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("numberOfConnections", equalTo(newMaxNumberOfConnections));
    }

    @Test
    public void testSetAndGetDefaultNumberOfConnectionsForProxyRepositoryWithTextAcceptHeader()
    {
        int newDefaultNumberOfConnections = 5;

        String url =
                getContextBaseUrl() + "/api/configuration/proxy/connection-pool/default/" + newDefaultNumberOfConnections;

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo("Default number of connections for proxy repository was updated successfully."));

        url = getContextBaseUrl() + "/api/configuration/proxy/connection-pool/default-number";

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo(String.valueOf(newDefaultNumberOfConnections)));
    }

    @Test
    public void testSetAndGetDefaultNumberOfConnectionsForProxyRepositoryWithJsonAcceptHeader()
    {
        int newDefaultNumberOfConnections = 5;

        String url =
                getContextBaseUrl() + "/api/configuration/proxy/connection-pool/default/" + newDefaultNumberOfConnections;

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message",
                     equalTo("Default number of connections for proxy repository was updated successfully."));

        url = getContextBaseUrl() + "/api/configuration/proxy/connection-pool/default-number";

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("numberOfConnections", equalTo(newDefaultNumberOfConnections));
    }

    @Test
    public void testSetAndGetNumberOfConnectionsForProxyRepositoryWithTextAcceptHeader()
    {
        Configuration configuration = configurationManager.getConfiguration();
        Optional<Repository> repositoryOpt = configuration.getStorages()
                                                          .values()
                                                          .stream()
                                                          .filter(stg -> MapUtils.isNotEmpty(stg.getRepositories()))
                                                          .flatMap(stg -> stg.getRepositories().values().stream())
                                                          .filter(repository ->
                                                                          repository.getRemoteRepository() != null &&
                                                                          repository.getRemoteRepository().getUrl() !=
                                                                          null)
                                                          .findAny();

        Repository repository = repositoryOpt.get();
        int numberOfConnections = 5;

        String url = getContextBaseUrl() + "/api/configuration/proxy/connection-pool/" +
                     repository.getStorage().getId() + "/" +
                     repository.getId() + "/" +
                     numberOfConnections;

        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(equalTo("Number of pool connections for repository was updated successfully."));

        url = getContextBaseUrl() + "/api/configuration/proxy/connection-pool/" +
              repository.getStorage().getId() + "/" +
              repository.getId();

        PoolStats expectedPoolStats = new PoolStats(0, 0, 0, numberOfConnections);
        given().header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body(containsString("max: " + expectedPoolStats.getMax()));
    }

    @Test
    public void testSetAndGetNumberOfConnectionsForProxyRepositoryWithJsonAcceptHeader()
    {
        Configuration configuration = configurationManager.getConfiguration();
        Optional<Repository> repositoryOpt = configuration.getStorages()
                                                          .values()
                                                          .stream()
                                                          .filter(stg -> MapUtils.isNotEmpty(stg.getRepositories()))
                                                          .flatMap(stg -> stg.getRepositories().values().stream())
                                                          .filter(repository ->
                                                                          repository.getRemoteRepository() != null &&
                                                                          repository.getRemoteRepository().getUrl() !=
                                                                          null)
                                                          .findAny();

        Repository repository = repositoryOpt.get();
        int numberOfConnections = 5;

        String url = getContextBaseUrl() + "/api/configuration/proxy/connection-pool/" +
                     repository.getStorage().getId() + "/" +
                     repository.getId() + "/" +
                     numberOfConnections;

        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("message", equalTo("Number of pool connections for repository was updated successfully."));

        url = getContextBaseUrl() + "/api/configuration/proxy/connection-pool/" +
              repository.getStorage().getId() + "/" +
              repository.getId();

        PoolStats expectedPoolStats = new PoolStats(0, 0, 0, numberOfConnections);
        given().header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
               .when()
               .get(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value())
               .body("max", equalTo(expectedPoolStats.getMax()));
    }
}
