package org.carlspring.strongbox.rest.common;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.carlspring.strongbox.rest.client.RestAssuredArtifactClient.OK;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.testing.TestCaseWithNugetPackageGeneration;
import org.carlspring.strongbox.users.domain.Roles;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.context.WebApplicationContext;

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

    @Before
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

        //noinspection ResultOfMethodCallIgnored
        file.delete();
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
        assertTrue("Path " + url + " doesn't exist.", pathExists(url));
    }

    public void createStorage(String storageId)
            throws IOException, JAXBException
    {
        createStorage(new Storage(storageId));
    }

    public void createStorage(Storage storage)
            throws IOException, JAXBException
    {
        configurationManagementService.saveStorage(storage);
        storageManagementService.createStorage(storage);
    }

    public void createRepository(Repository repository)
            throws IOException, JAXBException, RepositoryManagementStrategyException
    {
        configurationManagementService.saveRepository(repository.getStorage().getId(), repository);

        // Create the repository
        repositoryManagementService.createRepository(repository.getStorage().getId(), repository.getId());
    }
    
    public byte[] readPackageContent(Path packageFilePath)
        throws IOException
    {
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();

        MultipartEntityBuilder.create()
                              .addBinaryBody("package", Files.newInputStream(packageFilePath))
                              .setBoundary("---------------------------123qwe")
                              .build()
                              .writeTo(contentStream);
        contentStream.flush();

        byte[] packageContent = contentStream.toByteArray();

        return packageContent;
    }

}
