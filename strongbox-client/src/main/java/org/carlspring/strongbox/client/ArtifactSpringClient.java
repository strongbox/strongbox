package org.carlspring.strongbox.client;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.omg.CORBA.portable.InputStream;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

/**
 * Created by yury on 9/8/16.
 */
public class ArtifactSpringClient {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ArtifactSpringClient.class);
    protected String username = "maven";
    protected String password = "password";
    private String protocol = "http";
    private String host =
            System.getProperty("strongbox.host") != null ? System.getProperty("strongbox.host") : "localhost";
    private int port = System.getProperty("strongbox.port") != null ?
            Integer.parseInt(System.getProperty("strongbox.port")) :
            48080;
    private String contextBaseUrl;
    private RestTemplate restTemplate = new RestTemplate();


    public static ArtifactSpringClient getTestInstance() {
        return getTestInstance("maven", "password");
    }

    public static ArtifactSpringClient getTestInstanceLoggedInAsAdmin() {
        return getTestInstance("admin", "password");
    }

    public static ArtifactSpringClient getTestInstance(String username,
                                                       String password) {
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
            throws ArtifactOperationException {
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
            throws ArtifactOperationException {
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" + path;

        logger.debug("Deploying " + url + "...");

        deployMetadata(is, url, path, path.substring(path.lastIndexOf("/")));
    }

    public void deployFile(InputStream is,
                           String url,
                           String path,
                           String fileName)
            throws ArtifactOperationException {
        put(is, url, path, fileName, MediaType.APPLICATION_OCTET_STREAM);
    }

    public void deployMetadata(InputStream is,
                               String url,
                               String path,
                               String fileName)
            throws ArtifactOperationException {
        put(is, url, path, fileName, MediaType.APPLICATION_XML);
    }

    public void put(InputStream is,
                    String url,
                    String path,
                    String fileName,
                    MediaType mediaType)
            throws ArtifactOperationException {
        String contentDisposition = "attachment; filename=\"" + fileName + "\"";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.add("Content-Disposition", contentDisposition);

        HttpEntity<InputStream> entity = new HttpEntity<InputStream>(is, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class, path);

        handleFailures(response, "Failed to upload file!");
    }

    public void getArtifact(Artifact artifact,
                            String repository)
            throws ArtifactTransportException,
            IOException {
        String url = getContextBaseUrl() + "/" + repository + "/" + ArtifactUtils.convertArtifactToPath(artifact);

        logger.debug("Getting " + url + "...");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<Integer> entity = new HttpEntity<Integer>(headers);
        ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, entity, Resource.class);

        java.io.InputStream is;
        try {
            is = response.getBody().getInputStream();
        } catch (IOException e) {
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

    public java.io.InputStream getResource(String path)
            throws ArtifactTransportException,
            IOException {
        return getResource(path, 0);
    }

    public java.io.InputStream getResource(String path,
                                           long offset)
            throws ArtifactTransportException,
            IOException {
        String url = getContextBaseUrl() + (!path.startsWith("/") ? "/" : "") + path;

        logger.debug("Getting " + url + "...");

        ResponseEntity<Resource> response;

        if (offset > 0) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Range", "bytes=" + offset + "-");
            HttpEntity<Integer> entity = new HttpEntity<Integer>(headers);
            response = restTemplate.exchange(url, HttpMethod.GET, entity, Resource.class);
        } else {
            response = restTemplate.getForEntity(url, Resource.class);
        }

        java.io.InputStream is;
        try {
            is = response.getBody().getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return is;
    }

    public boolean pathExists(String path, String url) {
        logger.debug("Path to artifact: " + url);

        path = (path.startsWith("/") ? path : '/' + path);

        Map<String, String> vars = new HashMap<>();
        vars.put("path", path);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<Integer> entity = new HttpEntity<Integer>(headers);
        ResponseEntity response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, path);

        return response.getStatusCode().value() == 200;
    }

    private void handleFailures(ResponseEntity response,
                                String message)
            throws ArtifactOperationException, AuthenticationServiceException {

        int status = response.getStatusCode().value();

        if (status == SC_UNAUTHORIZED || status == SC_FORBIDDEN) {
            // TODO Handle authentication exceptions in a right way
            throw new AuthenticationServiceException(message +
                    "\nUser is unauthorized to execute that operation. " +
                    "Check assigned roles and privileges.");
        } else if (status != 200) {
            StringBuilder messageBuilder = new StringBuilder();
            messageBuilder.append("\n ERROR ").append(status).append(" ").append(message).append("\n");
            Object entity = response.getBody().toString();
            if (entity != null) {
                messageBuilder.append(entity.toString());
            }
            logger.error(messageBuilder.toString());
        }
    }


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getContextBaseUrl() {
        if (contextBaseUrl == null) {
            contextBaseUrl = protocol + "://" + host + ":" + port;
        }

        return contextBaseUrl;
    }

    public void setContextBaseUrl(String contextBaseUrl) {
        this.contextBaseUrl = contextBaseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
