package org.carlspring.strongbox.rest.client;

import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.client.BaseArtifactClient;
import org.carlspring.strongbox.client.IArtifactClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.google.common.io.ByteStreams;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.module.mockmvc.response.MockMvcResponse;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import io.restassured.response.ExtractableResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/**
 * Implementation of {@link IArtifactClient} for rest-assured tests.
 *
 * @author Alex Oreshkevich
 */
public class RestAssuredArtifactClient
        extends BaseArtifactClient
{

    /**
     * Default validation policy for GET requests.
     */
    public final static boolean VALIDATE_RESOURCE_ON_GET = false;

    public final static int OK = HttpStatus.OK.value();

    public final static int PARTIAL_CONTENT = HttpStatus.PARTIAL_CONTENT.value();

    private String contextBaseUrl;

    private String userAgent;

    private final MockMvcRequestSpecification mockMvc;
    
    public RestAssuredArtifactClient(MockMvcRequestSpecification mockMvc)
    {
        this.mockMvc = mockMvc;
    }

    @Override
    public String getContextBaseUrl()
    {
        return contextBaseUrl;
    }

    public void setContextBaseUrl(String contextBaseUrl)
    {
        this.contextBaseUrl = contextBaseUrl;
    }

    @Override
    public boolean pathExists(String path)
    {
        String url = escapeUrl(path);

        logger.debug("Path to artifact: {}", url);

        return givenLocal().contentType(ContentType.TEXT)
                           .when()
                           .get(url)
                           .getStatusCode() == OK;
    }

    @Override
    public InputStream getResource(String path,
                                   long offset)
            throws ArtifactTransportException, IOException
    {
        return getResource(path, (int) offset);
    }

    public void deployFile(InputStream is,
                           String url,
                           String fileName)
            throws ArtifactOperationException
    {
        put(is, url, fileName, ContentType.BINARY.toString());
    }

    public void deployMetadata(InputStream is,
                               String url,
                               String fileName)
            throws ArtifactOperationException
    {
        put(is, url, fileName, ContentType.BINARY.toString());
    }

    @Override
    public void put(InputStream is,
                    String url,
                    String fileName,
                    String mediaType)
            throws ArtifactOperationException
    {
        String contentDisposition = "attachment; filename=\"" + fileName + "\"";
        byte[] bytes;
        try
        {
            bytes = ByteStreams.toByteArray(is);
        }
        catch (IOException e)
        {
            throw new ArtifactOperationException("Unable to convert to byte array", e);
        }

        logger.debug("Deploying {}", url);

        givenLocal().contentType(mediaType)
                    .header("Content-Disposition", contentDisposition)
                    .header("filename", fileName)
                    .body(bytes)
                    .when()
                    .put(url)
                    .peek()
                    .then()
                    .statusCode(HttpStatus.OK.value());
    }

    private MockMvcRequestSpecification givenLocal()
    {
        if (userAgent != null)
        {
            return mockMvc.header("User-Agent", userAgent);
        }
        
        return mockMvc;
    }

    public MockMvcResponse put2(String relativeUrl,
                                Object body,
                                String mediaType)
    {
        return givenLocal().contentType(mediaType).body(body).when().put(relativeUrl).peek();
    }

    public InputStream getResource(String url)
    {
        return getResource(url, -1, VALIDATE_RESOURCE_ON_GET);
    }

    public InputStream getResource(String url,
                                   boolean validate)
    {
        return getResource(url, -1, validate);
    }

    public InputStream getResource(String url,
                                   int offset,
                                   boolean validate)
    {
        byte[] bytes = getArtifactAsByteArray(url, offset, validate);
        if (bytes == null)
        {
            return null;
        }
        else
        {
            return new ByteArrayInputStream(bytes);
        }
    }

    public InputStream getResource(String url,
                                   int offset)
    {
        return getResource(url, offset, VALIDATE_RESOURCE_ON_GET);
    }

    /**
     * Converts response output to byte array to properly use it later as a stream.
     * RestAssured-specific case for working with file uploading when multipart specification is not used.
     */
    public byte[] getArtifactAsByteArray(String url,
                                         int offset,
                                         boolean validate)
    {
        MockMvcRequestSpecification o = givenLocal().contentType(MediaType.TEXT_PLAIN_VALUE);
        int statusCode = OK;
        if (offset != -1)
        {
            o = o.header("Range", "bytes=" + offset + "-");
            statusCode = PARTIAL_CONTENT;
        }

        logger.debug("[getArtifactAsByteArray] URL {}", url);

        MockMvcResponse response = o.when().get(url);
        Headers allHeaders = response.getHeaders();

        logger.debug("HTTP GET {}", url);
        logger.debug("Response headers:");

        allHeaders.forEach(header -> logger.debug("\t{} = {}", header.getName(), header.getValue()));

        if (validate)
        {
            response.then().statusCode(statusCode);
        }

        if (response.getStatusCode() == OK || response.getStatusCode() == PARTIAL_CONTENT)
        {
            byte[] result = response.getMockHttpServletResponse().getContentAsByteArray();
            logger.debug("Received {} bytes.", result.length);
            return result;
        }
        else
        {
            logger.warn("[getArtifactAsByteArray] response {}", response.getStatusCode());
            return null;
        }
    }

    public void copy(String path,
                     String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId)
    {
        givenLocal().contentType(MediaType.TEXT_PLAIN_VALUE)
                    .params("srcStorageId", srcStorageId,
                            "srcRepositoryId", srcRepositoryId,
                            "destStorageId", destStorageId,
                            "destRepositoryId", destRepositoryId)
                    .when()
                    .post(getContextBaseUrl() + "/storages/copy/" + path)
                    .then()
                    .statusCode(OK);
    }

    public void delete(String storageId,
                       String repositoryId,
                       String path)
            throws ArtifactOperationException
    {
        delete(storageId, repositoryId, path, false);
    }

    public void delete(String storageId,
                       String repositoryId,
                       String path,
                       boolean force)
    {
        String url = getContextBaseUrl() + "/storages/" +
                     storageId + "/" + repositoryId + "/" + path;

        givenLocal().contentType(MediaType.TEXT_PLAIN_VALUE)
                    .param("force", force)
                    .when()
                    .delete(url)
                    .peek()
                    .then()
                    .statusCode(OK);
    }

    public ExtractableResponse getResourceWithResponse(String path,
                                                       String pathVar)
    {
        String url = getContextBaseUrl() + "/" + path;
        if (pathVar != null && !pathVar.isEmpty())
        {
            url += "/" + pathVar;
        }

        return givenLocal().accept(MediaType.TEXT_PLAIN_VALUE)
                           .when()
                           .get(url)
                           .peek()
                           .then()
                           .extract();
    }

    public void rebuildMetadata(String storageId,
                                String repositoryId,
                                String path)
    {
        String url = getContextBaseUrl() + "/api/maven/metadata?" + (storageId != null ? "storageId=" + storageId : "") +
                     (repositoryId != null ? (storageId != null ? "&" : "") + "repositoryId=" + repositoryId : "") +
                     (path != null ? (storageId != null || repositoryId != null ? "&" : "") + "path=" + path : "");

        givenLocal().contentType(MediaType.TEXT_PLAIN_VALUE)
                    .when()
                    .post(url)
                    .peek()
                    .then()
                    .statusCode(OK);
    }

    public void removeVersionFromMetadata(String storageId,
                                          String repositoryId,
                                          String artifactPath,
                                          String version,
                                          String classifier,
                                          String metadataType)
    {
        String url = getContextBaseUrl() + "/api/maven/metadata/" +
                     storageId + "/" + repositoryId + "/" +
                     (artifactPath != null ? artifactPath : "");

        givenLocal().contentType(MediaType.TEXT_PLAIN_VALUE)
                    .params("version", version,
                            "classifier", classifier,
                            "metadataType", metadataType)
                    .when()
                    .delete(url)
                    .peek()
                    .then()
                    .statusCode(OK);
    }

    public String search(String query,
                         String mediaType,
                         String searchProvider)
            throws UnsupportedEncodingException
    {
        return search(null, query, mediaType, searchProvider);
    }

    public String search(String repositoryId,
                         String query,
                         String mediaType,
                         String searchProvider)
            throws UnsupportedEncodingException
    {
        String url = getContextBaseUrl() + "/api/search";

        if (repositoryId == null)
        {
            repositoryId = "";
        }
        else
        {
            repositoryId = URLEncoder.encode(repositoryId, "UTF-8");
        }

        query = URLEncoder.encode(query, "UTF-8");

        return givenLocal().params("repositoryId", repositoryId,
                                   "q", query,
                                   "searchProvider", searchProvider)
                           .header("accept", mediaType)
                           .when()
                           .get(url)
                           .then()
                           .statusCode(OK)
                           .extract()
                           .response()
                           .getBody()
                           .asString();
    }

    public Headers getHeadersfromHEAD(String url)
    {
        MockMvcRequestSpecification o = givenLocal().contentType(MediaType.TEXT_PLAIN_VALUE);

        MockMvcResponse response = o.when().head(url);
        Headers allHeaders = response.getHeaders();

        logger.debug("HTTP HEAD {}", url);
        logger.debug("Response headers:");

        allHeaders.forEach(header -> logger.debug("\t{} = {}", header.getName(), header.getValue()));

        if (response.getStatusCode() == OK || response.getStatusCode() == PARTIAL_CONTENT)
        {
            logger.debug("Received headers successfully.");
            return allHeaders;
        }
        else
        {
            logger.warn("[getArtifactAsByteArray] response {}", response.getStatusCode());
            return null;
        }
    }

    public Headers getHeadersFromGET(String url)
    {
        MockMvcRequestSpecification o = givenLocal().contentType(MediaType.TEXT_PLAIN_VALUE);

        MockMvcResponse response = o.when().get(url);
        Headers allHeaders = response.getHeaders();

        logger.debug("HTTP GET {}", url);
        logger.debug("Response headers:");

        allHeaders.forEach(header -> logger.debug("\t{} = {}", header.getName(), header.getValue()));

        if (response.getStatusCode() == OK || response.getStatusCode() == PARTIAL_CONTENT)
        {
            logger.debug("Received headers successfully.");
            return allHeaders;
        }
        else
        {
            logger.warn("[getArtifactAsByteArray] response {}", response.getStatusCode());
            return null;
        }
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

}
