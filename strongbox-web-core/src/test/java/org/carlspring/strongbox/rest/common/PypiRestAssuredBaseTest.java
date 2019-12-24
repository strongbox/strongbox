package org.carlspring.strongbox.rest.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;

import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.users.domain.Privileges;

import javax.inject.Inject;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.context.WebApplicationContext;

import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;

/**
 * 
 * @author ankit.tomar
 *
 */
public abstract class PypiRestAssuredBaseTest
{

    protected static final String REPOSITORY_RELEASES = "pypi-releases-test";

    protected static final String REPOSITORY_STORAGE = "storage-pypi-test";

    protected static final String TEST_RESOURCES = "target/test-resources";

    protected static final String PIP_USER_AGENT = "pip/*";

    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Inject
    protected WebApplicationContext context;

    @Inject
    protected RestAssuredArtifactClient client;

    @Value("${strongbox.url}")
    private String contextBaseUrl;

    @Inject
    protected MockMvcRequestSpecification mockMvc;

    public void init()
        throws Exception
    {
        client.setUserAgent(PIP_USER_AGENT);
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
        logger.trace("[pathExists] URL -> {}", url);

        return mockMvc.header(HttpHeaders.USER_AGENT, PIP_USER_AGENT)
                      .contentType(MediaType.TEXT_PLAIN_VALUE)
                      .when()
                      .get(url)
                      .getStatusCode() == OK;
    }

    protected void assertPathExists(String url)
    {
        assertThat(pathExists(url)).as("Path " + url + " doesn't exist.").isTrue();
    }
}
