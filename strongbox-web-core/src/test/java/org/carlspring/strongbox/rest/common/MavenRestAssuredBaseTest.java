package org.carlspring.strongbox.rest.common;

import org.carlspring.strongbox.artifact.generator.MavenArtifactDeployer;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.testing.MavenTestCaseWithArtifactGeneration;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;
import org.carlspring.strongbox.users.domain.Privileges;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.context.WebApplicationContext;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    protected RepositoryPathResolver repositoryPathResolver;

    @Value("${strongbox.url}")
    private String contextBaseUrl;

    public void init()
            throws Exception
    {
        client.setUserAgent("Maven/*");
        client.setContextBaseUrl(contextBaseUrl);
    }

    public void shutdown()
    {
    }

    public String getContextBaseUrl()
    {
        return contextBaseUrl;
    }

    public void setContextBaseUrl(String contextBaseUrl)
    {
        this.contextBaseUrl = contextBaseUrl;
    }

    protected Collection<? extends GrantedAuthority> provideAuthorities()
    {
        return Privileges.all();
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
        assertTrue(pathExists(url), "Path " + url + " doesn't exist.");
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

}
