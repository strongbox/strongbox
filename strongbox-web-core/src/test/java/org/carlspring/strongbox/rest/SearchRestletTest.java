package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactDeployer;
import org.carlspring.strongbox.client.ArtifactClient;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mtodorov
 */
public class SearchRestletTest
{

    public static boolean INITIALIZED = false;

    private static ArtifactClient client;


    @Before
    public void setUp()
            throws Exception
    {
        if (!INITIALIZED)
        {
            client = new ArtifactClient();
            client.setUsername("maven");
            client.setPassword("password");
            client.setPort(48080);

            File strongboxBaseDir = new File("target/strongbox/tmp");
            String[] classifiers = new String[] { "javadoc", "tests" };

            Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3");
            Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3.1");
            Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3.2");

            ArtifactDeployer artifactDeployer = new ArtifactDeployer(strongboxBaseDir);
            artifactDeployer.generateAndDeployArtifact(artifact1, classifiers, "storage0", "releases");
            artifactDeployer.generateAndDeployArtifact(artifact2, classifiers, "storage0", "releases");
            artifactDeployer.generateAndDeployArtifact(artifact3, classifiers, "storage0", "releases");

            INITIALIZED = true;
        }
    }

    @Test
    public void testSearchXML()
            throws Exception
    {
        String response = client.search("g:org.carlspring.maven a:test-project", "xml");

        Assert.assertTrue("Received unexpected response!",
                          response.contains(">1.0.11.3<") && response.contains(">1.0.11.3.1<"));
    }

    @Test
    public void testSearchJSON()
            throws Exception
    {
        String response = client.search("g:org.carlspring.maven a:test-project", "json");

        Assert.assertTrue("Received unexpected response!",
                          response.contains("\"version\" : \"1.0.11.3\"") &&
                          response.contains("\"version\" : \"1.0.11.3.1\""));
    }

    @Test
    public void testSearchPlainText()
            throws Exception
    {
        String response = client.search("g:org.carlspring.maven a:test-project", "text");

        Assert.assertTrue("Received unexpected response!",
                          response.contains("org.carlspring.maven:test-project:1.0.11.3:pom") &&
                          response.contains("org.carlspring.maven:test-project:1.0.11.3.1:pom"));
    }

}
