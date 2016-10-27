package org.carlspring.strongbox.rest.common;

import org.carlspring.strongbox.artifact.generator.ArtifactDeployer;
import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.carlspring.strongbox.users.domain.Roles;

import java.io.File;
import java.util.Collection;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.context.WebApplicationContext;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.booters.StorageBooter.createTempDir;

/**
 * General settings for the testing subsystem.
 *
 * @author Alex Oreshkevich
 */
public abstract class RestAssuredBaseTest
{

    public final static int DEFAULT_PORT = 48080;
    public final static String DEFAULT_HOST = "localhost";

    /**
     * Share logger instance across all tests.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    @Autowired
    protected RestAssuredArtifactClient client;

    private String host;

    private int port;

    private String contextBaseUrl;

    private TestCaseWithArtifactGeneration generator = new TestCaseWithArtifactGeneration();

    public RestAssuredBaseTest()
    {

        // initialize host
        host = System.getProperty("strongbox.host");
        if (host == null)
        {
            host = DEFAULT_HOST;
        }

        // initialize port
        String strongboxPort = System.getProperty("strongbox.port");
        if (strongboxPort == null)
        {
            port = DEFAULT_PORT;
        }
        else
        {
            port = Integer.parseInt(strongboxPort);
        }

        // initialize base URL
        contextBaseUrl = "http://" + host + ":" + port;
    }

    @Before
    public void init()
    {
        RestAssuredMockMvc.webAppContextSetup(context);
        createTempDir();

        // security settings for tests
        // by default all operations incl. deletion etc. are allowed (careful)
        // override #provideAuthorities if you wanna be more specific
        anonymousAuthenticationFilter.getAuthorities().addAll(provideAuthorities());

        // base URL depends only on test execution context
        client.setContextBaseUrl(contextBaseUrl);
    }

    @After
    public void shutdown()
    {
        RestAssuredMockMvc.reset();
    }

    public String getContextBaseUrl()
    {
        return contextBaseUrl;
    }

    protected Collection<? extends GrantedAuthority> provideAuthorities()
    {
        return Roles.ADMIN.getPrivileges();
    }

    public static void removeDir(String path)
    {
        removeDir(new File(path));
    }

    /**
     * Recursively removes directory or file #file and all it's content.
     *
     * @param file directory or file to be removed
     */
    public static void removeDir(File file)
    {

        if (file == null || !file.exists())
        {
            return;
        }

        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            if (files != null)
            {
                for (File f : files)
                {
                    removeDir(f);
                }
            }

        }

        file.delete();
    }

    protected void assertPathExists(String url)
    {
        given().contentType(MediaType.TEXT_PLAIN_VALUE).when().get(url).then().statusCode(HttpStatus.OK.value());
    }

    protected ArtifactDeployer buildArtifactDeployer(File file)
    {
        ArtifactDeployer artifactDeployer = new ArtifactDeployer(file.getAbsolutePath());
        artifactDeployer.setClient(client);
        return artifactDeployer;
    }

    public String createSnapshotVersion(String baseSnapshotVersion,
                                        int buildNumber)
    {
        return generator.createSnapshotVersion(baseSnapshotVersion, buildNumber);
    }
}