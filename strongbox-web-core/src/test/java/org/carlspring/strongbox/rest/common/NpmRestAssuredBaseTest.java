package org.carlspring.strongbox.rest.common;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import javax.inject.Inject;

import org.apache.commons.io.output.NullOutputStream;
import org.carlspring.commons.io.MultipleDigestOutputStream;
import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.users.domain.Privileges;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author carlspring
 */
public abstract class NpmRestAssuredBaseTest
{

    protected static final String TEST_RESOURCES = "target/test-resources";

    /**
     * Share logger instance across all tests.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Inject
    protected WebApplicationContext context;

    @Inject
    protected RestAssuredArtifactClient client;

    @Value("${strongbox.url}")
    private String contextBaseUrl;

    public void init()
            throws Exception
    {
        client.setUserAgent("npm/*");
        client.setContextBaseUrl(contextBaseUrl);
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

    protected boolean pathExists(String url)
    {
        logger.trace("[pathExists] URL -> " + url);

        return given().header("user-agent", "npm/*")
                      .contentType(MediaType.TEXT_PLAIN_VALUE)
                      .when()
                      .get(url)
                      .getStatusCode() == OK;
    }

    protected void assertPathExists(String url)
    {
        assertTrue(pathExists(url), "Path " + url + " doesn't exist.");
    }

    protected void resolveArtifact(String artifactPath)
            throws NoSuchAlgorithmException, IOException
    {
        String url = getContextBaseUrl() + artifactPath;

        logger.debug("Requesting " + url + "...");

        InputStream is = client.getResource(url);
        if (is == null)
        {
            fail("Failed to resolve " + artifactPath + "!");
        }

        int total = 0;
        try (OutputStream fos = new NullOutputStream();
                MultipleDigestOutputStream mdos = new MultipleDigestOutputStream(fos))
        {

            int len;
            final int size = 1024;
            byte[] bytes = new byte[size];

            while ((len = is.read(bytes, 0, size)) != -1)
            {
                mdos.write(bytes, 0, len);
                total += len;
            }

            mdos.flush();
        }

        assertTrue(total > 0, "Resolved a zero-length artifact!");
    }

}
