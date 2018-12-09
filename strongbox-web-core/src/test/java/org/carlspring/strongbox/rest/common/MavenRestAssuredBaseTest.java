package org.carlspring.strongbox.rest.common;

import org.carlspring.strongbox.artifact.generator.MavenArtifactDeployer;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.testing.MavenMetadataServiceHelper;
import org.carlspring.strongbox.users.domain.Privileges;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.context.WebApplicationContext;

import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;

import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * General settings for the testing sub-system.
 *
 * @author Alex Oreshkevich
 */
public abstract class MavenRestAssuredBaseTest
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

    @Inject
    protected ConfigurationManagementService configurationManagementService;

    @Inject
    protected MavenMetadataServiceHelper mavenMetadataServiceHelper;

    @Value("${strongbox.url}")
    private String contextBaseUrl;

    @Inject
    protected MockMvcRequestSpecification mockMvc;
    
    public void init()
            throws Exception
    {
        client.setUserAgent("Maven/*");
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

        return mockMvc.header(HttpHeaders.USER_AGENT, "Maven/*")
                      .contentType(MediaType.TEXT_PLAIN_VALUE)
                      .when()
                      .get(url)
                      .getStatusCode() == OK;
    }

    protected void assertPathExists(String url)
    {
        assertThat(pathExists(url)).as("Path " + url + " doesn't exist.").isTrue();
    }

    protected MavenArtifactDeployer buildArtifactDeployer(Path path)
    {
        MavenArtifactDeployer deployer = new MavenArtifactDeployer(path.toString());
        deployer.setClient(client);
        return deployer;
    }
}
