package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.controllers.context.IntegrationTest;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import com.jayway.restassured.response.ExtractableResponse;
import org.apache.commons.collections.MapUtils;
import org.apache.http.pool.PoolStats;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertEquals;

@Ignore // This test needs to be re-worked after the changes in SB-728 and SB-729 were introduced.
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class HttpConnectionPoolConfigurationManagementControllerTest
        extends RestAssuredBaseTest
{

    @Inject
    private ConfigurationManager configurationManager;

    @BeforeClass
    public static void setUp()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        File storageBasedir = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0");

/*
        generateArtifact(storageBasedir.getAbsolutePath(),
                         "org.carlspring.strongbox:strongbox-utils:8.2:jar",
                         "1.0");
*/
    }

    @Test
    public void testGetMaxNumberOfConnectionsForProxyRepository()
    {

        String url = getContextBaseUrl() + "/configuration/proxy/connection-pool";

        ExtractableResponse response = given().contentType(MediaType.TEXT_PLAIN_VALUE)
                                              .when()
                                              .get(url)
                                              .peek()
                                              .then()
                                              .statusCode(200)
                                              .extract();

        String str = response.response().getBody().asString();

        assertEquals(Integer.valueOf(200), Integer.valueOf(str));
    }

    @Test
    public void testSetMaxNumberOfConnectionsForProxyRepository()
    {
        String url = getContextBaseUrl() + "/configuration/proxy/connection-pool/max/200";

        ExtractableResponse response = given().contentType(MediaType.TEXT_PLAIN_VALUE)
                                              .when()
                                              .put(url)
                                              .peek()
                                              .then()
                                              .statusCode(200)
                                              .extract();

        assertEquals("Max number of connections for proxy repository was updated successfully.",
                     response.response().getBody().asString());
    }

    @Test
    public void testGetDefaultNumberOfConnectionsForProxyRepository()
    {

        String url = getContextBaseUrl() + "/configuration/proxy/connection-pool/default-number";


        ExtractableResponse response = given().contentType(MediaType.TEXT_PLAIN_VALUE)
                                              .when()
                                              .get(url)
                                              .peek()
                                              .then()
                                              .statusCode(200)
                                              .extract();

        String str = response.response().getBody().asString();

        assertEquals(Integer.valueOf(5), Integer.valueOf(str));
    }

    @Test
    public void testSetDefaultNumberOfConnectionsForProxyRepository()
    {
        String url = getContextBaseUrl() + "/configuration/proxy/connection-pool/default/5";

        ExtractableResponse response = given().contentType(MediaType.TEXT_PLAIN_VALUE)
                                              .when()
                                              .put(url)
                                              .peek()
                                              .then()
                                              .statusCode(200)
                                              .extract();

        assertEquals("Default number of connections for proxy repository was updated successfully.",
                     response.response().getBody().asString());
    }

    @Test
    public void testSetNumberOfConnectionsForProxyRepository()
    {
        org.carlspring.strongbox.configuration.Configuration configuration = configurationManager.getConfiguration();
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

        String url = getContextBaseUrl() + "/configuration/proxy/connection-pool/" +
                     repository.getStorage().getId() + "/" +
                     repository.getId() + "/5";

        ExtractableResponse response = given().contentType(MediaType.TEXT_PLAIN_VALUE)
                                              .when()
                                              .put(url)
                                              .peek()
                                              .then()
                                              .statusCode(200)
                                              .extract();

        assertEquals("Number of pool connections for repository was updated successfully.",
                     response.response().getBody().asString());
    }

    @Ignore
    @Test
    public void testGetNumberOfConnectionsForProxyRepository()
    {
        org.carlspring.strongbox.configuration.Configuration configuration = configurationManager.getConfiguration();
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

        String url = getContextBaseUrl() + "/configuration/proxy/connection-pool/" +
                     repository.getStorage().getId() + "/" +
                     repository.getId();

        ExtractableResponse response = given().contentType(MediaType.TEXT_PLAIN_VALUE)
                                              .when()
                                              .get(url)
                                              .peek()
                                              .then()
                                              .statusCode(200)
                                              .extract();

        assertEquals(new PoolStats(0, 0, 0, 5).toString(), response.response().getBody().asString());
    }

}
