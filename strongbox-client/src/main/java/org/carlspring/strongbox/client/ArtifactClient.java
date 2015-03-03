package org.carlspring.strongbox.client;

import org.carlspring.maven.commons.util.ArtifactUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import org.apache.maven.artifact.Artifact;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class ArtifactClient
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactClient.class);

    public static final String MANAGEMENT_URL = "/manage/artifact";

    private String protocol = "http";

    private String host = "localhost";

    private int port = System.getProperty("port.jetty.listen") != null ?
                       Integer.parseInt(System.getProperty("port.jetty.listen")) :
                       48080;

    private String contextBaseUrl;

    private String username = "maven";

    private String password = "password";


    public ArtifactClient()
    {
    }

    public static ArtifactClient getTestInstance()
    {
        int port = System.getProperty("port.jetty.listen") != null ?
                   Integer.parseInt(System.getProperty("port.jetty.listen")) :
                   48080;

        ArtifactClient client = new ArtifactClient();
        client.setUsername("maven");
        client.setPassword("password");
        client.setPort(port);
        client.setContextBaseUrl("http://localhost:" + client.getPort());

        return client;
    }

    public void addArtifact(Artifact artifact,
                            String storageId,
                            String repositoryId,
                            InputStream is)
            throws ArtifactOperationException
    {
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" +
                     ArtifactUtils.convertArtifactToPath(artifact);

        logger.debug("Deploying " + url + "...");

        String fileName = ArtifactUtils.getArtifactFileName(artifact);

        deployFile(is, url, fileName);
    }

    public void deployFile(InputStream is,
                           String url,
                           String fileName)
            throws ArtifactOperationException
    {
        put(is, url, fileName, MediaType.APPLICATION_OCTET_STREAM);
    }

    public void put(InputStream is,
                    String url,
                    String fileName,
                    String mediaType)
            throws ArtifactOperationException
    {
        String contentDisposition = "attachment; filename=\"" + fileName +"\"";

        Client client = ClientBuilder.newClient();
        WebTarget resource = client.target(url);
        setupAuthentication(resource);

        Response response = resource.request(mediaType)
                                    .header("Content-Disposition", contentDisposition)
                                    .put(Entity.entity(is, mediaType));

        int status = response.getStatus();

        if (status != 200)
        {
            throw new ArtifactOperationException("Failed to upload file!");
        }
    }

    /**
     * This method will deploy an artifact with a random length to the remote host.
     * NOTE: This artifacts file will not be a valid Maven one, but will exist for the sake of testing.
     *
     * @param artifact
     * @param repositoryId
     * @param length
     * @throws ArtifactOperationException
     */
    public void addArtifact(Artifact artifact,
                            String repositoryId,
                            long length)
            throws ArtifactOperationException
    {
        Client client = ClientBuilder.newClient();

        String url = getContextBaseUrl() + MANAGEMENT_URL + "/" +
                     repositoryId + "/state/EXISTS/length/" + length + "/" +
                     ArtifactUtils.convertArtifactToPath(artifact);

        logger.debug("Using " + url);

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);
        Response response = webResource.request(MediaType.TEXT_PLAIN).get();

        int status = response.getStatus();

        if (status != 200)
        {
            throw new ArtifactOperationException("Failed to create artifact!");
        }
    }

    public void getArtifact(Artifact artifact,
                            String repository)
            throws ArtifactTransportException,
                   IOException
    {
        Client client = ClientBuilder.newClient();

        String url = getContextBaseUrl() + "/" + repository + "/" + ArtifactUtils.convertArtifactToPath(artifact);

        logger.debug("Getting " + url + "...");

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);
        Response response = webResource.request(MediaType.TEXT_PLAIN).get();

        final InputStream is = response.readEntity(InputStream.class);

        int total = 0;
        int len;
        final int size = 4096;
        byte[] bytes = new byte[size];

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            total += len;
        }

        logger.debug("Response code: " + response.getStatus() + ". Read: " + total + " bytes.");

        int status = response.getStatus();

        if (status != 200)
        {
            throw new ArtifactTransportException("Failed to resolve artifact!");
        }

        if (total == 0)
        {
            throw new ArtifactTransportException("Artifact size was zero!");
        }
    }

    public void deleteArtifact(Artifact artifact,
                               String storageId,
                               String repositoryId)
    {
        Client client = ClientBuilder.newClient();

        String url = getUrlForArtifact(artifact, storageId, repositoryId);

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);
        webResource.request().delete();
    }

    public void delete(String storageId,
                       String repositoryId,
                       String path)
    {
        delete(storageId, repositoryId, path, false);
    }

    public void delete(String storageId,
                       String repositoryId,
                       String path,
                       boolean force)
    {
        Client client = ClientBuilder.newClient();

        @SuppressWarnings("ConstantConditions")
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" + path +
                     (force ? "?force=" + force : "");

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);
        webResource.request().delete();
    }

    public void deleteTrash(String storageId, String repositoryId)
    {
        Client client = ClientBuilder.newClient();

        String url = getUrlForTrash(storageId, repositoryId);

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);
        webResource.request().delete();
    }

    public void deleteTrash()
    {
        Client client = ClientBuilder.newClient();

        String url = getContextBaseUrl() + "/trash";

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);
        webResource.request().delete();
    }

    public boolean artifactExists(Artifact artifact,
                                  String storageId,
                                  String repositoryId)
            throws ResponseException
    {
        Response response = artifactExistsStatusCode(artifact, storageId, repositoryId);

        if (response.getStatus() == 200)
        {
            return true;
        }
        else if (response.getStatus() == 404)
        {
            return false;
        }
        else
        {
            throw new ResponseException(response.getStatusInfo().getReasonPhrase(), response.getStatus());
        }
    }

    public Response artifactExistsStatusCode(Artifact artifact,
                                                   String storageId,
                                                   String repositoryId)
            throws ResponseException
    {
        Client client = ClientBuilder.newClient();

        String url = getUrlForArtifact(artifact, storageId, repositoryId);

        logger.debug("Path to artifact: " + url);

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);
        return webResource.request(MediaType.APPLICATION_XML).get();
    }

    public boolean pathExists(String path)
    {
        Client client = ClientBuilder.newClient();

        String url = getContextBaseUrl() + (path.startsWith("/") ? path : '/' + path);

        logger.debug("Path to artifact: " + url);

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);
        Response response = webResource.request(MediaType.APPLICATION_XML).get();

        return response.getStatus() == 200;
    }

    public String getUrlForArtifact(Artifact artifact,
                                    String storageId,
                                    String repositoryId)
    {
        return getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" +
               ArtifactUtils.convertArtifactToPath(artifact);
    }

    public String getUrlForTrash(String storageId, String repositoryId)
    {
        return getContextBaseUrl() + "/trash/" + storageId + "/" + repositoryId;
    }

    public void setupAuthentication(WebTarget target)
    {
        if (username != null && password != null)
        {
            target.register(HttpAuthenticationFeature.basic(username, password));
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
