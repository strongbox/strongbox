package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactDeployer;
import org.carlspring.strongbox.client.RestClient;

import javax.ws.rs.core.MediaType;
import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mtodorov
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/META-INF/spring/strongbox-*-context.xml", "classpath*:/META-INF/spring/strongbox-*-context.xml"})
public class SearchRestletTest
{

    public static boolean INITIALIZED = false;

    private static RestClient client = new RestClient();


    @Before
    public void setUp()
            throws Exception
    {
        if (!INITIALIZED)
        {
            File strongboxBaseDir = new File("target/strongbox/tmp");
            String[] classifiers = new String[] { "javadoc", "tests" };

            Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3");
            Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3.1");
            Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3.2");

            ArtifactDeployer artifactDeployer = new ArtifactDeployer(strongboxBaseDir);
            artifactDeployer.setClient(client);
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
        String response = client.search("g:org.carlspring.maven a:test-project", MediaType.APPLICATION_XML_TYPE);

        System.out.println(response);

        Assert.assertTrue("Received unexpected response!",
                          response.contains(">1.0.11.3<") && response.contains(">1.0.11.3.1<"));
    }

    @Test
    public void testSearchJSON()
            throws Exception
    {
        String response = client.search("g:org.carlspring.maven a:test-project", MediaType.APPLICATION_JSON_TYPE);

        System.out.println(response);

        Assert.assertTrue("Received unexpected response!",
                          response.contains("\"version\" : \"1.0.11.3\"") &&
                          response.contains("\"version\" : \"1.0.11.3.1\""));
    }

    @Test
    public void testSearchPlainText()
            throws Exception
    {
        String response = client.search("g:org.carlspring.maven a:test-project", MediaType.TEXT_PLAIN_TYPE);

        System.out.println(response);

        Assert.assertTrue("Received unexpected response!",
                          response.contains("org.carlspring.maven:test-project:1.0.11.3:jar") &&
                          response.contains("org.carlspring.maven:test-project:1.0.11.3.1:jar"));
    }

}
