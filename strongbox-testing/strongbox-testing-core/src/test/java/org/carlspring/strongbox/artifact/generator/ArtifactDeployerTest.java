package org.carlspring.strongbox.artifact.generator;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.TestingCoreConfig;
import org.carlspring.strongbox.client.ArtifactClient;
import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.testing.AssignedPorts;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestingCoreConfig.class)
public class ArtifactDeployerTest
{

    private final static File BASEDIR = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases/.temp");

    @Autowired
    private AssignedPorts assignedPorts;

    private static ArtifactClient client;


    @Before
    public void setUp()
            throws Exception
    {
        if (!BASEDIR.exists())
        {
            //noinspection ResultOfMethodCallIgnored
            BASEDIR.mkdirs();

            client = new ArtifactClient();
            client.setUsername("maven");
            client.setPassword("password");
            client.setPort(assignedPorts.getPort("port.jetty.listen"));
            client.setContextBaseUrl("http://localhost:" + client.getPort());
        }
    }

    @After
    public void tearDown()
            throws Exception
    {
        if (client != null)
        {
            client.close();
        }
    }

    @Test
    public void testArtifactDeployment()
            throws ArtifactOperationException,
                   IOException,
                   NoSuchAlgorithmException,
                   XmlPullParserException
    {
        Artifact artifact = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox:test:1.2.3");

        String[] classifiers = new String[] { "javadocs", "jdk14", "tests"};

        ArtifactDeployer artifactDeployer = new ArtifactDeployer(BASEDIR);
        artifactDeployer.setClient(client);
        artifactDeployer.generateAndDeployArtifact(artifact, classifiers, "storage0", "releases", "jar");
    }

}
