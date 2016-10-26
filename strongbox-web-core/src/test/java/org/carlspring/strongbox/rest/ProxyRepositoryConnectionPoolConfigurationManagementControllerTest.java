package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import com.jayway.restassured.response.ExtractableResponse;
import org.apache.commons.collections.MapUtils;
import org.apache.http.pool.PoolStats;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertEquals;
import static org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration.generateArtifact;

/**
 * Created by yury on 8/25/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
public class ProxyRepositoryConnectionPoolConfigurationManagementControllerTest
        extends RestAssuredBaseTest
{

    private static final File STORAGE_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                         "/storages/storage0");

    @Autowired
    private ConfigurationManager configurationManager;


    @Before
    public void setUp()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        generateArtifact(STORAGE_BASEDIR.getAbsolutePath(),
                         "org.carlspring.strongbox:strongbox-utils:8.2:jar",
                         "1.0");
    }

    @Test
    public void testGetMaxNumberOfConnectionsForProxyRepository()
    {

        String url = getContextBaseUrl() + "/configuration/proxy/connection-pool";

        ExtractableResponse response = given()
                                               .contentType(MediaType.TEXT_PLAIN_VALUE)
                                               .when()
                                               .get(url)
                                               .peek()
                                               .then()
                                               .statusCode(200).extract();

        String str = response.response().getBody().asString();

        assertEquals(Integer.valueOf(200), Integer.valueOf(str));

    }

    @Test
    public void testSetMaxNumberOfConnectionsForProxyRepository()
    {
        String url = getContextBaseUrl() + "/configuration/proxy/connection-pool/max/200";

        ExtractableResponse response = given()
                                               .contentType(MediaType.TEXT_PLAIN_VALUE)
                                               .when()
                                               .put(url)
                                               .peek()
                                               .then()
                                               .statusCode(200).extract();

        Assert.assertEquals("Max number of connections for proxy repository was updated successfully.",
                            response.response().getBody().asString());
    }

    @Test
    public void testGetDefaultNumberOfConnectionsForProxyRepository()
    {

        String url = getContextBaseUrl() + "/configuration/proxy/connection-pool/default-number";


        ExtractableResponse response = given()
                                               .contentType(MediaType.TEXT_PLAIN_VALUE)
                                               .when()
                                               .get(url)
                                               .peek()
                                               .then()
                                               .statusCode(200).extract();

        String str = response.response().getBody().asString();
        Assert.assertEquals(Integer.valueOf(5), Integer.valueOf(str));
    }

    @Test
    public void testSetDefaultNumberOfConnectionsForProxyRepository()
    {
        String url = getContextBaseUrl() + "/configuration/proxy/connection-pool/default/5";

        ExtractableResponse response = given()
                                               .contentType(MediaType.TEXT_PLAIN_VALUE)
                                               .when()
                                               .put(url)
                                               .peek()
                                               .then()
                                               .statusCode(200).extract();

        Assert.assertEquals("Default number of connections for proxy repository was updated successfully.",
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

        ExtractableResponse response = given()
                                               .contentType(MediaType.TEXT_PLAIN_VALUE)
                                               .when()
                                               .put(url)
                                               .peek()
                                               .then()
                                               .statusCode(200).extract();

        Assert.assertEquals("Number of pool connections for repository was updated successfully.",
                            response.response().getBody().asString());
    }

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

        String url =
                getContextBaseUrl() + "/configuration/proxy/connection-pool/" + repository.getStorage().getId() + "/" +
                repository.getId();

        ExtractableResponse response = given()
                                               .contentType(MediaType.TEXT_PLAIN_VALUE)
                                               .when()
                                               .get(url)
                                               .peek()
                                               .then()
                                               .statusCode(200).extract();

        Assert.assertEquals(new PoolStats(0, 0, 0, 5).toString(), response.response().getBody().asString());
    }

}
