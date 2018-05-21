package org.carlspring.strongbox.rest.common;

import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.storage.repository.RawRepositoryFactory;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.repository.RepositoryTypeEnum;
import org.carlspring.strongbox.storage.repository.remote.RemoteRepository;
import org.carlspring.strongbox.testing.TestCaseWithRepositoryManagement;
import org.carlspring.strongbox.users.domain.Roles;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collection;

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
 * @author carlspring
 */
public class RawRestAssuredBaseTest
        extends TestCaseWithRepositoryManagement
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
    RawRepositoryFactory rawRepositoryFactory;

    private String contextBaseUrl;

    @Before
    public void init()
            throws Exception
    {
        client.setUserAgent("Raw/*");
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
        assertTrue("Path " + url + " doesn't exist.", pathExists(url));
    }

    @Override
    public void createProxyRepository(String storageId,
                                      String repositoryId,
                                      String remoteRepositoryUrl)
            throws IOException,
                   JAXBException,
                   RepositoryManagementStrategyException
    {
        RemoteRepository remoteRepository = new RemoteRepository();
        remoteRepository.setUrl(remoteRepositoryUrl);

        Repository repository = rawRepositoryFactory.createRepository(storageId, repositoryId);
        repository.setType(RepositoryTypeEnum.PROXY.getType());
        repository.setRemoteRepository(remoteRepository);

        createRepository(repository);
    }

}
