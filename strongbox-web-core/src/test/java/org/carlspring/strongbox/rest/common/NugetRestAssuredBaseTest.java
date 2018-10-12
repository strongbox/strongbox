package org.carlspring.strongbox.rest.common;

import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.testing.TestCaseWithNugetPackageGeneration;
import org.carlspring.strongbox.users.domain.Roles;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.context.WebApplicationContext;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * General settings for the testing sub-system.
 *
 * @author Alex Oreshkevich
 */
public abstract class NugetRestAssuredBaseTest
        extends TestCaseWithNugetPackageGeneration
{

    public final static int DEFAULT_PORT = 48080;

    public final static String DEFAULT_HOST = "localhost";

    /**
     * Share logger instance across all tests.
     */
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    @Inject
    protected ConfigurationManagementService configurationManagementService;

    @Inject
    protected RepositoryManagementService repositoryManagementService;

    @Inject
    protected StorageManagementService storageManagementService;

    @Inject
    protected WebApplicationContext context;

    @Inject
    private AnonymousAuthenticationFilter anonymousAuthenticationFilter;

    @Inject
    protected RestAssuredArtifactClient client;

    private String contextBaseUrl;

    @BeforeEach
    public void init()
            throws Exception
    {
        logger.debug("Initializing RestAssured...");

        // Security settings for tests:
        // By default all operations incl. deletion, etc. are allowed (be careful)!
        // Override #provideAuthorities, if you want be more specific.
        anonymousAuthenticationFilter.getAuthorities().addAll(provideAuthorities());

        client.setUserAgent("NuGet/*");
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

        return given().header("user-agent", "Nuget/*")
                      .contentType(MediaType.TEXT_PLAIN_VALUE)
                      .when()
                      .get(url)
                      .getStatusCode() == OK;
    }

    protected void assertPathExists(String url)
    {
        assertTrue(pathExists(url), "Path " + url + " doesn't exist.");
    }

    public void createStorage(String storageId)
            throws IOException, JAXBException
    {
        createStorage(new MutableStorage(storageId));
    }

    public void createStorage(MutableStorage storage)
            throws IOException, JAXBException
    {
        configurationManagementService.saveStorage(storage);
        storageManagementService.createStorage(storage);
    }

    public void createRepository(String storageId,
                                 MutableRepository repository)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        configurationManagementService.saveRepository(storageId, repository);

        // Create the repository
        repositoryManagementService.createRepository(storageId, repository.getId());
    }
    
    public byte[] readPackageContent(Path packageFilePath)
        throws IOException
    {
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();

        MultipartEntityBuilder.create()
                              .addBinaryBody("package", new BufferedInputStream(Files.newInputStream(packageFilePath)))
                              .setBoundary("---------------------------123qwe")
                              .build()
                              .writeTo(contentStream);
        contentStream.flush();

        byte[] packageContent = contentStream.toByteArray();

        return packageContent;
    }

}
