package org.carlspring.strongbox.rest.common;

import org.carlspring.maven.artifact.downloader.IndexDownloader;
import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.testing.TestCaseWithArtifactGeneration;
import org.carlspring.strongbox.users.domain.Roles;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.context.WebApplicationContext;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static junit.framework.TestCase.assertTrue;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;

/**
 * General settings for the testing cron tasks.
 *
 * @author Kate Novik.
 */
public class RestAssuredIndexDownloader
        extends IndexDownloader
{

    private static final Logger logger = LoggerFactory.getLogger(RestAssuredIndexDownloader.class);

    public final static int DEFAULT_PORT = 48080;

    public final static String DEFAULT_HOST = "localhost";

    @Autowired
    protected WebApplicationContext context;

    @Autowired
    AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    @Autowired
    protected RestAssuredArtifactClient client;

    @Autowired
    protected ObjectMapper objectMapper;

    private String host;

    private int port;

    private String contextBaseUrl;

    private TestCaseWithArtifactGeneration generator = new TestCaseWithArtifactGeneration();

    public RestAssuredIndexDownloader()
            throws PlexusContainerException, ComponentLookupException
    {
        logger.debug("Initialized RestAssuredIndexDownloader.");

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
        logger.info("Initialize and start web application server...");

        RestAssuredMockMvc.webAppContextSetup(context);

        // security settings for tests
        // by default all operations incl. deletion etc. are allowed (careful)
        // override #provideAuthorities if you wanna be more specific
        anonymousAuthenticationFilter.getAuthorities()
                                     .addAll(provideAuthorities());

        setContextBaseUrl(contextBaseUrl);
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
        assertTrue("Path " + url + " doesn't exists.", pathExists(url));
    }

    public void createArtifact(String basedir,
                               Artifact artifact)
            throws NoSuchAlgorithmException, XmlPullParserException, IOException
    {
        generator.generateArtifact(basedir, artifact);
    }
}
