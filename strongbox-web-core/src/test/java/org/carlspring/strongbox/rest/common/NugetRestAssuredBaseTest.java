package org.carlspring.strongbox.rest.common;

import org.carlspring.strongbox.rest.client.RestAssuredArtifactClient;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;

import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.WebApplicationContext;

import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;

/**
 * General settings for the testing sub-system.
 *
 * @author Alex Oreshkevich
 */
public abstract class NugetRestAssuredBaseTest
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
    protected RestAssuredArtifactClient client;

    @Value("${strongbox.url}")
    private String contextBaseUrl;

    @Inject
    protected MockMvcRequestSpecification mockMvc;
    
    public void init()
            throws Exception
    {
        logger.debug("Initializing RestAssured...");

        client.setUserAgent("NuGet/*");
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

        return contentStream.toByteArray();
    }

}
