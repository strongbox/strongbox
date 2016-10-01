package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.generator.SpringArtifactDeployer;
import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.storage.indexing.SearchResults;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import java.io.File;
import java.net.URLEncoder;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.apache.maven.artifact.Artifact;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Created by yury on 9/6/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
@WithUserDetails("admin")
public class SearchControllerTest
        extends BackendBaseTest
{

    @BeforeClass
    public static void setUp()
            throws Exception
    {
            File strongboxBaseDir = new File(ConfigurationResourceResolver.getVaultDirectory() + "/tmp");
            String[] classifiers = new String[]{ "javadoc",
                                                 "tests" };

            Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3");
            Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3.1");
            Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3.2");

            SpringArtifactDeployer artifactDeployer = new SpringArtifactDeployer(strongboxBaseDir);
/*
            artifactDeployer.generateAndDeployArtifact(artifact1, classifiers, "storage0", "releases", "jar");
            artifactDeployer.generateAndDeployArtifact(artifact2, classifiers, "storage0", "releases", "jar");
            artifactDeployer.generateAndDeployArtifact(artifact3, classifiers, "storage0", "releases", "jar");*/
    }

    @Test
    public void testSearchXML()
            throws Exception
    {
        String repositoryId = null;
        String query = "g:org.carlspring.maven a:test-project";

        String url = getContextBaseUrl() + "/search";
        repositoryId = (repositoryId != null ? "repositoryId=" + URLEncoder.encode(repositoryId, "UTF-8") : "");
        String q = URLEncoder.encode(query, "UTF-8");

        //noinspection UnnecessaryLocalVariable
        String response = RestAssuredMockMvc.given()
                                            .contentType(MediaType.TEXT_PLAIN_VALUE)
                                            .params("repositoryId", repositoryId, "q", q)
                                            .header("accept", MediaType.TEXT_PLAIN_VALUE)
                                            .when()
                                            .get(url)
                                            .peek() // Use peek() to print the ouput
                                            .then()
                                            .statusCode(200).extract().response().getBody().asString();

        GenericParser<SearchResults> parser = new GenericParser<>(SearchResults.class);
        SearchResults serializedProxyConfiguration = parser.deserialize(response);

        System.out.println(serializedProxyConfiguration);
        /*Assert.assertTrue("Received unexpected response!",
                response.contains(">1.0.11.3<") && response.contains(">1.0.11.3.1<"));*/
    }

}
