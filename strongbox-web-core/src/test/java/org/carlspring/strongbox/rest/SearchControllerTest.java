package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.ArtifactDeployer;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.rest.context.IntegrationTest;

import java.io.File;

import org.apache.maven.artifact.Artifact;
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
        // Don't ever change this check in order "to make the tests omnipotent".
        // They are sharing the same resources and this is good enough test data which *will not* change during testing.
        File repositoryBasedir = new File(ConfigurationResourceResolver.getVaultDirectory() + "/storages/storage0/releases");
        if (new File(repositoryBasedir, "org/carlspring/strongbox/searches/test-project").exists())
        {
            return;
        }

        File strongboxBaseDir = new File(ConfigurationResourceResolver.getVaultDirectory() + "/tmp");
        String[] classifiers = new String[]{ "javadoc", "tests" };

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.searches:test-project:1.0.11.3");
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.searches:test-project:1.0.11.3.1");
        Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.strongbox.searches:test-project:1.0.11.3.2");

        ArtifactDeployer artifactDeployer = buildArtifactDeployer(strongboxBaseDir);

        artifactDeployer.generateAndDeployArtifact(artifact1, classifiers, "storage0", "releases", "jar");
        artifactDeployer.generateAndDeployArtifact(artifact2, classifiers, "storage0", "releases", "jar");
        artifactDeployer.generateAndDeployArtifact(artifact3, classifiers, "storage0", "releases", "jar");
    }

    @Test
    public void testSearchPlainText()
            throws Exception
    {
        String response = client.search("g:org.carlspring.strongbox.searches a:test-project", MediaType.TEXT_PLAIN_VALUE);

        logger.debug(response);

        assertTrue("Received unexpected response!",
                   response.contains("org.carlspring.strongbox.searches:test-project:1.0.11.3:jar") &&
                   response.contains("org.carlspring.strongbox.searches:test-project:1.0.11.3.1:jar"));
    }

    @Test
    public void testSearchJSON()
            throws Exception
    {
        String response = client.search("g:org.carlspring.strongbox.searches a:test-project", MediaType.APPLICATION_JSON_VALUE);

        System.out.println(response);

        assertTrue("Received unexpected response!",
                   response.contains("\"version\" : \"1.0.11.3\"") &&
                   response.contains("\"version\" : \"1.0.11.3.1\""));
    }

    @Test
    public void testSearchXML()
            throws Exception
    {
        String response = client.search("g:org.carlspring.strongbox.searches a:test-project", MediaType.APPLICATION_XML_VALUE);

        System.out.println(response);

        assertTrue("Received unexpected response!",
                   response.contains(">1.0.11.3<") && response.contains(">1.0.11.3.1<"));
    }

}
