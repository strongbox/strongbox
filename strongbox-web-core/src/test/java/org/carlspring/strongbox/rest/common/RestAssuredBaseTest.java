package org.carlspring.strongbox.rest.common;

import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.users.domain.Roles;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;

import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.jayway.restassured.module.mockmvc.response.MockMvcResponse;
import com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import com.jayway.restassured.response.ExtractableResponse;
import com.jayway.restassured.response.Headers;
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

    private String host;

    private int port;

    private String contextBaseUrl;

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

    protected InputStream getArtifactAsStream(String url)
    {
        return getArtifactAsStream(url, -1);
    }

    protected InputStream getArtifactAsStream(String url,
                                              int offset)
    {
        return new ByteArrayInputStream(getArtifactAsByteArray(url, offset));
    }

    /**
     * Converts response output to byte array to properly use it later as a stream.
     * RestAssured-specific case for working with file uploading when multipart specification is not used.
     */
    protected byte[] getArtifactAsByteArray(String url,
                                            int offset)
    {
        MockMvcRequestSpecification o = given().contentType(MediaType.TEXT_PLAIN_VALUE);
        int statusCode = 200;
        if (offset != -1)
        {
            o = o.header("Range", "bytes=" + offset + "-");
            statusCode = 206;
        }

        logger.debug("[getArtifactAsByteArray] URL " + url);

        MockMvcResponse response = o.when().get(url);
        Headers allHeaders = response.getHeaders();
        logger.debug("HTTP GET " + url);
        logger.debug("Response headers:");

        allHeaders.forEach(header -> logger.trace("\t" + header.getName() + " = " + header.getValue()));

        response.then().statusCode(statusCode);
        byte[] result = response.getMockHttpServletResponse().getContentAsByteArray();

        logger.debug("Received " + result.length + " bytes.");

        return result;
    }

    protected void copy(String path,
                        String srcStorageId,
                        String srcRepositoryId,
                        String destStorageId,
                        String destRepositoryId)
    {
        given()
                .contentType(MediaType.TEXT_PLAIN_VALUE)
                .params("path", path,
                        "srcStorageId", srcStorageId,
                        "srcRepositoryId", srcRepositoryId,
                        "destStorageId", destStorageId,
                        "destRepositoryId", destRepositoryId)
                .when()
                .post(getContextBaseUrl() + "/storages/copy")
                .peek()
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract();
    }

    protected void delete(String storageId,
                          String repositoryId,
                          String path)
            throws ArtifactOperationException
    {
        delete(storageId, repositoryId, path, false);
    }

    protected void delete(String storageId,
                          String repositoryId,
                          String path,
                          boolean force)
            throws ArtifactOperationException
    {
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId;

        given().contentType(MediaType.TEXT_PLAIN_VALUE)
               .param("path", path)
               .param("force", force)
               .when()
               .delete(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    protected ExtractableResponse getResourceWithResponse(String path,
                                                          String pathVar)
    {
        String url = getContextBaseUrl() + "/" + path;
        if (pathVar != null && !pathVar.isEmpty())
        {
            url += "/" + pathVar;
        }

        return (ExtractableResponse) given().contentType(MediaType.TEXT_PLAIN_VALUE)
                                            .when()
                                            .get(url)
                                            .peek()
                                            .then()
                                            .extract();
    }
}