package org.carlspring.strongbox.rest;

import org.apache.commons.collections.MapUtils;
import org.apache.http.pool.PoolStats;

import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.context.RestletTestContext;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * @author korest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@RestletTestContext
public class ProxyRepositoryConnectionPoolConfigurationManagementRestletTest
        extends TestCaseWithArtifactGeneration
{

    @Configuration
    @ComponentScan(basePackages = { "org.carlspring.strongbox",
                                    "org.carlspring.logging" })
    public static class SpringConfig {}

    private static final RestClient restClient = new RestClient();

    private static final File STORAGE_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0");

    @Autowired
    private ConfigurationManager configurationManager;


    @BeforeClass
    public static void init()
            throws NoSuchAlgorithmException,
                   XmlPullParserException,
                   IOException
    {
        generateArtifact(STORAGE_BASEDIR.getAbsolutePath(),
                         "org.carlspring.strongbox:strongbox-utils:8.2:jar",
                         new String[] {"1.0"});
    }

    @AfterClass
    public static void tearDown()
    {
        if (restClient != null)
        {
            restClient.close();
        }
    }

    @Test
    public void testGetMaxNumberOfConnectionsForProxyRepository()
    {
        Response response = restClient.prepareTarget("/configuration/proxy/connection-pool").request().get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(Integer.valueOf(200), response.readEntity(Integer.class));
    }

    @Test
    public void testSetMaxNumberOfConnectionsForProxyRepository()
    {
        Response response = restClient.prepareTarget("/configuration/proxy/connection-pool/max/200")
                                      .request()
                                      .put(Entity.json(""));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Max number of connections for proxy repository was updated successfully.",
                     response.readEntity(String.class));
    }

    @Test
    public void testGetDefaultNumberOfConnectionsForProxyRepository()
    {
        Response response = restClient.prepareTarget("/configuration/proxy/connection-pool/default-number")
                                      .request()
                                      .get();
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(Integer.valueOf(5), response.readEntity(Integer.class));
    }

    @Test
    public void testSetDefaultNumberOfConnectionsForProxyRepository()
    {
        Response response = restClient.prepareTarget("/configuration/proxy/connection-pool/default/5").request()
                                      .put(Entity.json(""));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Default number of connections for proxy repository was updated successfully.",
                     response.readEntity(String.class));
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
                                                          .filter(repository -> repository.getRemoteRepository() != null &&
                                                                                repository.getRemoteRepository().getUrl() != null)
                                                          .findAny();

        Repository repository = repositoryOpt.get();

        Response response = restClient.prepareTarget("/configuration/proxy/connection-pool/" +
                                                     repository.getStorage().getId() + "/" +
                                                     repository.getId() + "/5")
                                      .request()
                                      .put(Entity.json(""));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Number of pool connections for repository was updated successfully.", response.readEntity(String.class));
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
                                                          .filter(repository -> repository.getRemoteRepository() != null &&
                                                                                repository .getRemoteRepository().getUrl() != null)
                                                          .findAny();

        Repository repository = repositoryOpt.get();

        Response response = restClient.prepareTarget("/configuration/proxy/connection-pool/" + repository.getStorage().getId() + "/" + repository.getId())
                                      .request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(new PoolStats(0, 0, 0, 5).toString(), response.readEntity(String.class));
    }

}
