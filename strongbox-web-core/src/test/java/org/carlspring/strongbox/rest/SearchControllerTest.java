package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactDeployer;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.junit.Assert.assertTrue;

/**
 * @author Alex Oreshkevich, Martin Todorov
 */
@IntegrationTest
@RunWith(SpringJUnit4ClassRunner.class)
public class SearchControllerTest
        extends RestAssuredBaseTest
{

    @Before
    public synchronized void setUp()
            throws Exception
    {
        if (isInitialized())
        {
            return; // storage/storage0/releases do not allow artifact redeployment
        }

        File strongboxBaseDir = new File(ConfigurationResourceResolver.getVaultDirectory() + "/tmp");
        String[] classifiers = new String[]{ "javadoc",
                                             "tests" };

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3");
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3.1");
        Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3.2");

        ArtifactDeployer artifactDeployer = buildArtifactDeployer(strongboxBaseDir);

        artifactDeployer.generateAndDeployArtifact(artifact1, classifiers, "storage0", "releases", "jar");
        artifactDeployer.generateAndDeployArtifact(artifact2, classifiers, "storage0", "releases", "jar");
        artifactDeployer.generateAndDeployArtifact(artifact3, classifiers, "storage0", "releases", "jar");
    }

    @Test
    public void testSearchPlainText()
            throws Exception
    {
        String response = client.search("g:org.carlspring.maven a:test-project", MediaType.TEXT_PLAIN_VALUE);

        logger.debug(response);

        assertTrue("Received unexpected response!",
                   response.contains("org.carlspring.maven:test-project:1.0.11.3:jar") &&
                   response.contains("org.carlspring.maven:test-project:1.0.11.3.1:jar"));
    }

    @Test
    public void testSearchJSON()
            throws Exception
    {
        String response = client.search("g:org.carlspring.maven a:test-project", MediaType.APPLICATION_JSON_VALUE);

        System.out.println(response);

        Assert.assertTrue("Received unexpected response!",
                          response.contains("\"version\" : \"1.0.11.3\"") &&
                          response.contains("\"version\" : \"1.0.11.3.1\""));
    }

    @Test
    public void testSearchXML()
            throws Exception
    {
        String response = client.search("g:org.carlspring.maven a:test-project", MediaType.APPLICATION_XML_VALUE);

        System.out.println(response);

        Assert.assertTrue("Received unexpected response!",
                          response.contains(">1.0.11.3<") && response.contains(">1.0.11.3.1<"));
    }

    private boolean isInitialized()
    {
        return new File(ConfigurationResourceResolver.getVaultDirectory() +
                        "/storages/storage0/releases/org/carlspring/maven/test-project").exists();
    }
}
