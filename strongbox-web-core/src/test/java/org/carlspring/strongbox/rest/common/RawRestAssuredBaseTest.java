package org.carlspring.strongbox.rest.common;

import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.users.domain.Privileges;

import javax.inject.Inject;
import java.util.Collection;

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
 * @author carlspring
 */
public class RawRestAssuredBaseTest
{

    protected static final String STORAGE0 = "storage0";
    
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
        client.setUserAgent("Raw/*");
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

        return given().header("user-agent", "Raw/*")
                      .contentType(MediaType.TEXT_PLAIN_VALUE)
                      .when()
                      .get(url)
                      .getStatusCode() == OK;
    }

    protected void assertPathExists(String url)
    {
        assertTrue(pathExists(url), "Path " + url + " doesn't exist.");
    }

}
