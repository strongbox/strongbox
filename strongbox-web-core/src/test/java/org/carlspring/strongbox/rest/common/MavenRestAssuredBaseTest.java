package org.carlspring.strongbox.rest.common;

import org.carlspring.maven.commons.util.ArtifactUtils;
import org.carlspring.strongbox.artifact.MavenArtifact;
import org.carlspring.strongbox.artifact.MavenArtifactUtils;
import org.carlspring.strongbox.artifact.MavenRepositoryArtifact;
import org.carlspring.strongbox.artifact.generator.MavenArtifactDeployer;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.testing.MavenTestCaseWithArtifactGeneration;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.users.domain.Roles;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.context.WebApplicationContext;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.junit.Assert.assertTrue;

/**
 * General settings for the testing sub-system.
 *
 * @author Alex Oreshkevich
 */
public abstract class MavenRestAssuredBaseTest
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    /**
     * Share logger instance across all tests.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Inject
    protected WebApplicationContext context;

    @Inject
    protected RestAssuredArtifactClient client;

    @Inject
    private RepositoryPathResolver repositoryPathResolver;
    
    private String contextBaseUrl;
    
    @Before
    public void init()
            throws Exception
    {
        client.setUserAgent("Maven/*");
    }
    
    @After
    public void shutdown()
    {
    }

    public String getContextBaseUrl()
    {
        return contextBaseUrl;
    }

    @Inject
    public void setContextBaseUrl(String contextBaseUrl)
    {
        this.contextBaseUrl = contextBaseUrl;

        // base URL depends only on test execution context
        client.setContextBaseUrl(contextBaseUrl);
    }

    protected Collection<? extends GrantedAuthority> provideAuthorities()
    {
        return Roles.ADMIN.getPrivileges();
    }

    /**
     * Recursively removes directory or file #file and all it's content.
     *
     * @param path directory or file to be removed
     */
    public static void removeDir(Path path)
            throws IOException
    {
        final List<Path> pathsToDelete = Files.walk(path).sorted(Comparator.reverseOrder()).collect(
                Collectors.toList());
        for (Path toDelete : pathsToDelete)
        {
            Files.deleteIfExists(toDelete);
        }
    }

    protected boolean pathExists(String url)
    {
        logger.trace("[pathExists] URL -> " + url);

        return given().header("user-agent", "Maven/*")
                      .contentType(MediaType.TEXT_PLAIN_VALUE)
                      .when()
                      .get(url)
                      .getStatusCode() == OK;
    }

    protected void assertPathExists(String url)
    {
        assertTrue("Path " + url + " doesn't exist.", pathExists(url));
    }

    protected MavenArtifactDeployer buildArtifactDeployer(Path path)
    {
        MavenArtifactDeployer deployer = new MavenArtifactDeployer(path.toString());
        deployer.setClient(client);
        return deployer;
    }

    public String createSnapshotVersion(String baseSnapshotVersion,
                                        int buildNumber)
    {
        return new MavenTestCaseWithArtifactGeneration().createSnapshotVersion(baseSnapshotVersion, buildNumber);
    }

    public MavenArtifact createTimestampedSnapshotArtifact(String repositoryBasedir,
                                                           String groupId,
                                                           String artifactId,
                                                           String baseSnapshotVersion,
                                                           String packaging,
                                                           String[] classifiers,
                                                           int numberOfBuilds)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        MavenArtifact artifact = null;

        for (int i = 0; i < numberOfBuilds; i++)
        {
            String version = createSnapshotVersion(baseSnapshotVersion, i + 1);

            artifact = new MavenRepositoryArtifact(groupId, artifactId, version);

            Path repositoryPath = Paths.get(repositoryBasedir);
            String repositoryId = repositoryPath.getFileName().toString();
            String storageId = repositoryPath.getParent().getFileName().toString();
            
            RepositoryPath artifactPath = repositoryPathResolver.resolve(storageId, repositoryId, MavenArtifactUtils.convertArtifactToPath(artifact));
            artifact.setPath(artifactPath);

            generateArtifact(repositoryBasedir, artifact, packaging);

            if (classifiers != null)
            {
                for (String classifier : classifiers)
                {
                    String gavtc = groupId + ":" + artifactId + ":" + version + ":jar:" + classifier;
                    generateArtifact(repositoryBasedir, ArtifactUtils.getArtifactFromGAVTC(gavtc));
                }
            }
        }

        // Return the main artifact
        return artifact;
    }

}
