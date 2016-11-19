package org.carlspring.strongbox.client;

import org.carlspring.maven.commons.util.ArtifactUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

/**
 * @author yury
 */
public class ArtifactSpringClient
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactSpringClient.class);

    protected String username = "maven";

    protected String password = "password";

    private String protocol = "http";

    private String host = System.getProperty("strongbox.host") != null ? System.getProperty("strongbox.host") : "localhost";

    private int port = System.getProperty("strongbox.port") != null ?
                       Integer.parseInt(System.getProperty("strongbox.port")) :
                       48080;

    private String contextBaseUrl;

    private RestTemplate restTemplate = new RestTemplate();


    public static ArtifactSpringClient getTestInstance()
    {
        return getTestInstance("maven", "password");
    }

    public static ArtifactSpringClient getTestInstanceLoggedInAsAdmin()
    {
        return getTestInstance("admin", "password");
    }

    public static ArtifactSpringClient getTestInstance(String username,
                                                       String password)
    {
        String host = System.getProperty("strongbox.host") != null ?
                      System.getProperty("strongbox.host") :
                      "localhost";

        int port = System.getProperty("strongbox.port") != null ?
                   Integer.parseInt(System.getProperty("strongbox.port")) :
                   48080;

        ArtifactSpringClient client = new ArtifactSpringClient();
        client.setUsername(username);
        client.setPassword(password);
        client.setPort(port);
        client.setContextBaseUrl("http://" + host + ":" + client.getPort());

        return client;
    }

    /* Not checked*/
    public void addArtifact(Artifact artifact,
                            String storageId,
                            String repositoryId,
                            InputStream is)
            throws ArtifactOperationException
    {
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId;

        logger.debug("Deploying " + url + "...");

        String fileName = ArtifactUtils.getArtifactFileName(artifact);
        String path = ArtifactUtils.convertArtifactToPath(artifact);

        deployFile(is, url, path, fileName);
    }

    public void addMetadata(Metadata metadata,
                            String path,
                            String storageId,
                            String repositoryId,
                            InputStream is)
            throws ArtifactOperationException
    {
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" + path;

        logger.debug("Deploying " + url + "...");

        deployMetadata(is, url, path, path.substring(path.lastIndexOf("/")));
    }

    public void deployFile(InputStream is,
                           String url,
                           String path,
                           String fileName)
            throws ArtifactOperationException
    {
        put(is, url, path, fileName, MediaType.APPLICATION_OCTET_STREAM);
    }

    public void deployMetadata(InputStream is,
                               String url,
                               String path,
                               String fileName)
            throws ArtifactOperationException
    {
        put(is, url, path, fileName, MediaType.APPLICATION_XML);
    }

    public void put(InputStream is,
                    String url,
                    String path,
                    String fileName,
                    MediaType mediaType)
            throws ArtifactOperationException
    {
        String contentDisposition = "attachment; filename=\"" + fileName + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.add("Content-Disposition", contentDisposition);

        byte[] bytes;
        try
        {
            bytes = IOUtils.toByteArray(is);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Parse error on put method, class ArtifactSpringClient...", e);
        }

        HttpEntity<byte[]> entity = new HttpEntity<>(bytes, headers);
        path = "/" + path;

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        try
        {
            params.add("path", path);
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);
        }

        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(url).queryParams(params).build();
        URI uri = uriComponents.toUri();

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.PUT, entity, String.class);

        handleFailures(response, "Failed to upload file!");
    }

    public void getArtifact(Artifact artifact,
                            String repository)
            throws ArtifactTransportException,
                   IOException
    {
        String url = getContextBaseUrl() + "/" + repository + "/" + ArtifactUtils.convertArtifactToPath(artifact);

        logger.debug("Getting " + url + "...");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<Integer> entity = new HttpEntity<Integer>(headers);
        ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, entity, Resource.class);

        java.io.InputStream is;
        try
        {
            is = response.getBody().getInputStream();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        int total = 0;
        int len;
        final int size = 4096;
        byte[] bytes = new byte[size];

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            total += len;
        }

        logger.debug("Response code: " + response.getStatusCode() + ". Read: " + total + " bytes.");

        int status = response.getStatusCode().value();
        if (status != 200)
        {
            throw new ArtifactTransportException("Failed to resolve artifact!");
        }

        if (total == 0)
        {
            throw new ArtifactTransportException("Artifact size was zero!");
        }
    }

    public java.io.InputStream getResource(String path,
                                           String url)
            throws ArtifactTransportException,
                   IOException
    {
        return getResource(path, url, 0);
    }

    public ResponseEntity getResourceWithResponse(String pathVar)
            throws ArtifactTransportException,
                   IOException
    {
        String path = "/storages/storage0/releases";
        String url = getContextBaseUrl() + (!path.startsWith("/") ? "/" : "") + path + ("?path=" + pathVar);

        logger.debug("Getting " + url + "...");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response;
    }

    public java.io.InputStream getResource(String path,
                                           String url,
                                           long offset)
            throws ArtifactTransportException,
                   IOException
    {

        path = (!path.startsWith("/") ? "/" : "") + path;
        url = getContextBaseUrl() + url + ("?path=" + path);

        logger.debug("Getting " + url + "...");

        ResponseEntity<Resource> response;

        if (offset > 0)
        {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Range", "bytes=" + offset + "-");
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            response = restTemplate.exchange(url, HttpMethod.GET, entity, Resource.class);
        }
        else
        {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            HttpEntity<String> entity = new HttpEntity<String>(headers);
            response = restTemplate.exchange(url, HttpMethod.GET, entity, Resource.class);
        }

        java.io.InputStream is;
        try
        {
            is = response.getBody().getInputStream();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return is;
    }

    // Not checked**
    public void deleteArtifact(Artifact artifact,
                               String storageId,
                               String repositoryId)
            throws ArtifactOperationException
    {
        String url = getUrlForArtifact(artifact, storageId, repositoryId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

        handleFailures(response, "Failed to delete artifact!");
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
        @SuppressWarnings("ConstantConditions")
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "?path=" + path +
                     (force ? "?force=" + force : "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

        handleFailures(response, "Failed to delete artifact!");
    }

    public void deleteTrash(String storageId,
                            String repositoryId)
            throws ArtifactOperationException
    {
        String url = getUrlForTrash(storageId, repositoryId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

        handleFailures(response, "Failed to delete the trash for " + storageId + ":" + repositoryId + "!");
    }

    public void deleteTrash()
            throws ArtifactOperationException
    {
        String url = getContextBaseUrl() + "/trash";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);

        handleFailures(response, "Failed to delete trash for all repositories!");
    }

    public String getUrlForArtifact(Artifact artifact,
                                    String storageId,
                                    String repositoryId)
    {
        return getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" +
               ArtifactUtils.convertArtifactToPath(artifact);
    }

    public String getUrlForTrash(String storageId,
                                 String repositoryId)
    {
        return getContextBaseUrl() + "/trash/" + storageId + "/" + repositoryId;
    }


    public boolean pathExists(String path,
                              String url)
    {

        path = (path.startsWith("/") ? path : '/' + path);
        url = getContextBaseUrl() + "/" + url + ("?path=" + path);

        logger.debug("Path to artifact: " + path + " URL " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<Integer> entity = new HttpEntity<Integer>(headers);

        ResponseEntity response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getStatusCode().value() == 200;
    }

    public void undelete(String storageId,
                         String repositoryId,
                         String path)
            throws ArtifactOperationException
    {
        @SuppressWarnings("ConstantConditions")
        String url = getUrlForTrash(storageId, repositoryId) + "?path=" + path;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>("Undelete", headers);
        ResponseEntity response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        handleFailures(response, "Failed to delete the trash for " + storageId + ":" + repositoryId + "!");
    }

    // Not checked**
    public void undeleteTrash(String storageId,
                              String repositoryId)
            throws ArtifactOperationException
    {
        String url = getUrlForTrash(storageId, repositoryId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>("Undelete", headers);
        ResponseEntity response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        handleFailures(response, "Failed to delete the trash for " + storageId + ":" + repositoryId + "!");
    }

    // НУЖНО ПРОТЕСТИТЬ
    public void undeleteTrash()
            throws ArtifactOperationException
    {
        String url = getContextBaseUrl() + "/trash";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        handleFailures(response, "Failed to delete the trash!");
    }

    public boolean artifactExists(Artifact artifact,
                                  String storageId,
                                  String repositoryId)
            throws ResponseException
    {
        ResponseEntity response = artifactExistsStatusCode(artifact, storageId, repositoryId);

        if (response.getStatusCode().value() == 200)
        {
            return true;
        }
        else if (response.getStatusCode().value() == 404)
        {
            return false;
        }
        else
        {
            throw new ResponseException(response.getStatusCode().toString(), response.getStatusCode().value());
        }
    }

    public ResponseEntity artifactExistsStatusCode(Artifact artifact,
                                                   String storageId,
                                                   String repositoryId)
            throws ResponseException
    {
        String url = getUrlForArtifact(artifact, storageId, repositoryId);

        logger.debug("Path to artifact: " + url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response;
    }

    private void handleFailures(ResponseEntity response,
                                String message)
            throws ArtifactOperationException, AuthenticationServiceException
    {

        int status = response.getStatusCode().value();

        if (status == SC_UNAUTHORIZED || status == SC_FORBIDDEN)
        {
            // TODO Handle authentication exceptions in a right way
            throw new AuthenticationServiceException(message +
                                                     "\nUser is unauthorized to execute that operation. " +
                                                     "Check assigned roles and privileges.");
        }
        else if (status != 200)
        {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("\n ERROR ").append(status).append(" ").append(message).append("\n");
            Object entity = response.getBody().toString();
            if (entity != null)
            {
                messageBuilder.append(entity.toString());
            }
            logger.error(messageBuilder.toString());
        }
    }


    public String getProtocol()
    {
        return protocol;
    }

    public void setProtocol(String protocol)
    {
        this.protocol = protocol;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    public String getContextBaseUrl()
    {
        if (contextBaseUrl == null)
        {
            contextBaseUrl = protocol + "://" + host + ":" + port;
        }

        return contextBaseUrl;
    }

    public void setContextBaseUrl(String contextBaseUrl)
    {
        this.contextBaseUrl = contextBaseUrl;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

}
