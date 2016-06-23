package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.client.RestClient;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.resource.ResourceCloser;
import org.carlspring.strongbox.rest.context.RestletTestContext;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.ws.rs.client.Entity;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

/**
 * @author korest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@RestletTestContext
public class ProxyRepositoryConnectionPoolConfigurationManagementRestletTest extends TestCaseWithArtifactGeneration {

    @Configuration
    @ComponentScan(basePackages = { "org.carlspring.strongbox",
            "org.carlspring.logging" })
    public static class SpringConfig
    {

    }

    private static final RestClient restClient = new RestClient();

    private static final File STORAGE_BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0");

    @BeforeClass
    public static void init() throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        restClient.getClientInstance().property(ClientProperties.SUPPRESS_HTTP_COMPLIANCE_VALIDATION, true);

        generateArtifact(STORAGE_BASEDIR.getAbsolutePath(),
                "org.carlspring.strongbox:strongbox-utils:8.2:jar",
                new String[]{ "1.0" });
    }

    @AfterClass
    public static void tearDown()
    {
        if(restClient != null)
        {
            restClient.close();
        }
    }

    @Test
    public void setNumberOfConnectionsForProxyRepositoryTest()
    {
        restClient.prepareTarget("/configuration/proxy/connection-pool/storage0/test-repo/5").request().put(Entity.json(null));
    }
}
