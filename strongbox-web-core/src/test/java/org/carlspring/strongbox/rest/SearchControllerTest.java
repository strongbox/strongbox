package org.carlspring.strongbox.rest;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.config.WebConfig;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.rest.common.RestAssuredBaseTest;
import org.carlspring.strongbox.services.ArtifactManagementService;
import org.carlspring.strongbox.storage.indexing.SearchResults;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.apache.maven.artifact.Artifact;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import static org.springframework.test.util.AssertionErrors.assertTrue;

/**
 * Created by yury on 9/6/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = WebConfig.class)
@WebAppConfiguration
@WithUserDetails("admin")
public class SearchControllerTest
        extends RestAssuredBaseTest
{

    @Autowired
    protected ArtifactManagementService artifactManagementService;

    private static final File REPOSITORY_BASEDIR_RELEASES = new File(ConfigurationResourceResolver.getVaultDirectory() +
                                                                     "/storages/storage0/releases");

    @BeforeClass
    public static void setUp()
            throws Exception
    {


        // TODO replace artifactDeployer.generateAndDeployArtifact()
        // with artifactManagementService.store(storageId, repositoryId, path, is);


      /*  SpringArtifactDeployer artifactDeployer = new SpringArtifactDeployer();
            artifactDeployer.generateAndDeployArtifact(artifact1, classifiers, "storage0", "releases", "jar");
            artifactDeployer.generateAndDeployArtifact(artifact2, classifiers, "storage0", "releases", "jar");
            artifactDeployer.generateAndDeployArtifact(artifact3, classifiers, "storage0", "releases", "jar");*/
    }

    @Test
    public void testSearchXML()
            throws Exception
    {
        File strongboxBaseDir = new File(ConfigurationResourceResolver.getVaultDirectory() + "/tmp");
        String[] classifiers = new String[]{ "javadoc",
                                             "tests" };

        Artifact artifact1 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3");
        Artifact artifact2 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3.1");
        Artifact artifact3 = ArtifactUtils.getArtifactFromGAVTC("org.carlspring.maven:test-project:1.0.11.3.2");

        File artifactFile = new File(REPOSITORY_BASEDIR_RELEASES, ArtifactUtils.convertArtifactToPath(artifact1));

        artifactManagementService.store("storage0", "releases", ArtifactUtils.convertArtifactToPath(artifact1),
                                        new FileInputStream(artifactFile));
        artifactManagementService.store("storage0", "releases", ArtifactUtils.convertArtifactToPath(artifact2),
                                        new FileInputStream(artifact2.getFile()));
        artifactManagementService.store("storage0", "releases", ArtifactUtils.convertArtifactToPath(artifact3),
                                        new FileInputStream(artifact3.getFile()));

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

        logger.debug(serializedProxyConfiguration.toString());
        assertTrue("Received unexpected response!",
                   response.contains(">1.0.11.3<") && response.contains(">1.0.11.3.1<"));
    }

}
