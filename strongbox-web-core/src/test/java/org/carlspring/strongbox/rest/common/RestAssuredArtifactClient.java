package org.carlspring.strongbox.rest.common;

import org.carlspring.strongbox.client.ArtifactOperationException;
import org.carlspring.strongbox.client.ArtifactTransportException;
import org.carlspring.strongbox.client.BaseArtifactClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.common.io.ByteStreams;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.module.mockmvc.response.MockMvcResponse;
import com.jayway.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import com.jayway.restassured.response.ExtractableResponse;
import com.jayway.restassured.response.Headers;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;

/**
 * @author Alex Oreshkevich
 */
@Component
public class RestAssuredArtifactClient
        extends BaseArtifactClient
{

    private String contextBaseUrl;

    @Override
    public String getContextBaseUrl()
    {
        return contextBaseUrl;
    }

    @Override
    public boolean pathExists(String path)
    {
        String url = escapeUrl(path);
        logger.debug("Path to artifact: " + url);
        return given().contentType(ContentType.TEXT)
                      .when()
                      .get(url)
                      .getStatusCode() == HttpStatus.OK.value();
    }

    @Override
    public InputStream getResource(String path,
                                   long offset)
            throws ArtifactTransportException, IOException
    {
        return getArtifactAsStream(path, (int) offset);
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
    protected void put(InputStream is,
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

        logger.debug("[put] url = " + url);

        given().contentType(mediaType)
               .header("Content-Disposition", contentDisposition)
               .body(bytes)
               .when()
               .put(url)
               .peek()
               .then()
               .statusCode(HttpStatus.OK.value());
    }

    void setContextBaseUrl(String contextBaseUrl)
    {
        this.contextBaseUrl = contextBaseUrl;
    }

    public InputStream getArtifactAsStream(String url)
    {
        return getArtifactAsStream(url, -1, false);
    }

    public InputStream getArtifactAsStream(String url,
                                           boolean validate)
    {
        return getArtifactAsStream(url, -1, validate);
    }

    public InputStream getArtifactAsStream(String url,
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

    public InputStream getArtifactAsStream(String url,
                                           int offset)
    {
        return getArtifactAsStream(url, offset, false);
    }

    /**
     * Converts response output to byte array to properly use it later as a stream.
     * RestAssured-specific case for working with file uploading when multipart specification is not used.
     */
    public byte[] getArtifactAsByteArray(String url,
                                         int offset,
                                         boolean validate)
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

        allHeaders.forEach(header -> logger.debug("\t" + header.getName() + " = " + header.getValue()));

        if (validate)
        {
            response.then().statusCode(statusCode);
        }

        if (response.getStatusCode() == 200 || response.getStatusCode() == 206)
        {
            byte[] result = response.getMockHttpServletResponse().getContentAsByteArray();
            logger.debug("Received " + result.length + " bytes.");
            return result;
        }
        else
        {
            logger.warn("[getArtifactAsByteArray] response " + response.getStatusCode());
            return null;
        }
    }

    public void copy(String path,
                     String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId)
    {
        given().contentType(MediaType.TEXT_PLAIN_VALUE)
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

    public ExtractableResponse getResourceWithResponse(String path,
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
