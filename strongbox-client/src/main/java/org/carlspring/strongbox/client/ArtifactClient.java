package org.carlspring.strongbox.client;

import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author mtodorov
 */
public class ArtifactClient implements Closeable
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactClient.class);

    public static final String MANAGEMENT_URL = "/manage/artifact";

    private String protocol = "http";

    private String host = System.getProperty("strongbox.host") != null ? System.getProperty("strongbox.host") : "localhost";

    private int port = System.getProperty("strongbox.port") != null ?
                       Integer.parseInt(System.getProperty("strongbox.port")) :
                       48080;

    private String contextBaseUrl;

    private String username = "maven";

    private String password = "password";

    private Client client;


    public ArtifactClient()
    {
    }

    public static ArtifactClient getTestInstance()
    {
        String host = System.getProperty("strongbox.host") != null ?
                      System.getProperty("strongbox.host") :
                      "localhost";

        int port = System.getProperty("strongbox.port") != null ?
                   Integer.parseInt(System.getProperty("strongbox.port")) :
                   48080;

        ArtifactClient client = new ArtifactClient();
        client.setUsername("maven");
        client.setPassword("password");
        client.setPort(port);
        client.setContextBaseUrl("http://" + host + ":" + client.getPort());

        return client;
    }

    public Client getClientInstance()
    {
        if (client == null)
        {
            ClientConfig config = getClientConfig();
            client = ClientBuilder.newClient(config);

            return client;
        }
        else
        {
            return client;
        }
    }

    private ClientConfig getClientConfig()
    {
        ClientConfig config = new ClientConfig();
        config.connectorProvider(new ApacheConnectorProvider());

        return config;
    }

    @Override
    public void close()
    {
        if (client != null)
        {
            client.close();
        }
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

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(mediaType)
                                    .header("Content-Disposition", contentDisposition)
                                    .put(Entity.entity(is, mediaType));

        handleFailures(response, "Failed to upload file!");
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
        String url = getContextBaseUrl() + MANAGEMENT_URL + "/" +
                     repositoryId + "/state/EXISTS/length/" + length + "/" +
                     ArtifactUtils.convertArtifactToPath(artifact);

        logger.debug("Using " + url);

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);
        Response response = resource.request(MediaType.TEXT_PLAIN).get();

        handleFailures(response, "Failed to create artifact!");
    }

    public void getArtifact(Artifact artifact,
                            String repository)
            throws ArtifactTransportException,
                   IOException
    {
        String url = getContextBaseUrl() + "/" + repository + "/" + ArtifactUtils.convertArtifactToPath(artifact);

        logger.debug("Getting " + url + "...");

        WebTarget webResource = getClientInstance().target(url);
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

    public InputStream getResource(String path)
            throws ArtifactTransportException,
                   IOException
    {
        return getResource(path, 0);
    }

    public InputStream getResource(String path, long offset)
            throws ArtifactTransportException,
                   IOException
    {
        String url = getContextBaseUrl() + (!path.startsWith("/") ? "/" : "") + path;

        logger.debug("Getting " + url + "...");

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Invocation.Builder request = resource.request();
        Response response;

        if (offset > 0)
        {
            response = request.header("Range", "bytes=" + offset + "-").get();
        }
        else
        {
            response = request.get();
        }

        return response.readEntity(InputStream.class);
    }

    public Response getResourceWithResponse(String path)
            throws ArtifactTransportException,
                   IOException
    {
        String url = getContextBaseUrl() + (!path.startsWith("/") ? "/" : "") + path;

        logger.debug("Getting " + url + "...");

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        return resource.request(MediaType.TEXT_PLAIN).get();
    }

    public void deleteArtifact(Artifact artifact,
                               String storageId,
                               String repositoryId)
            throws ArtifactOperationException
    {
        String url = getUrlForArtifact(artifact, storageId, repositoryId);

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request().delete();

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
        String url = getContextBaseUrl() + "/storages/" + storageId + "/" + repositoryId + "/" + path +
                     (force ? "?force=" + force : "");

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request().delete();

        handleFailures(response, "Failed to delete artifact!");
    }

    public void deleteTrash(String storageId, String repositoryId)
            throws ArtifactOperationException
    {
        String url = getUrlForTrash(storageId, repositoryId);

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request().delete();

        handleFailures(response, "Failed to delete the trash for " + storageId + ":" + repositoryId + "!");
    }

    public void deleteTrash()
            throws ArtifactOperationException
    {
        String url = getContextBaseUrl() + "/trash";

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request().delete();

        handleFailures(response, "Failed to delete trash for all repositories!");
    }

    public void undelete(String storageId,
                         String repositoryId,
                         String path)
            throws ArtifactOperationException
    {
        @SuppressWarnings("ConstantConditions")
        String url = getUrlForTrash(storageId, repositoryId) + "/" + path;

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN)
                                    .post(Entity.entity("Undelete", MediaType.TEXT_PLAIN));

        handleFailures(response, "Failed to delete the trash for " + storageId + ":" + repositoryId + "!");
    }

    public void undeleteTrash(String storageId, String repositoryId)
            throws ArtifactOperationException
    {
        String url = getUrlForTrash(storageId, repositoryId);

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN)
                                    .post(Entity.entity("Undelete", MediaType.TEXT_PLAIN));

        handleFailures(response, "Failed to delete the trash for " + storageId + ":" + repositoryId + "!");
    }

    public void undeleteTrash()
            throws ArtifactOperationException
    {
        String url = getContextBaseUrl() + "/trash";

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN)
                                    .post(Entity.entity("Undelete", MediaType.TEXT_PLAIN));

        handleFailures(response, "Failed to delete the trash!");
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
        String url = getUrlForArtifact(artifact, storageId, repositoryId);

        logger.debug("Path to artifact: " + url);

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        return resource.request(MediaType.TEXT_PLAIN).get();
    }

    public boolean pathExists(String path)
    {
        String url = getContextBaseUrl() + (path.startsWith("/") ? path : '/' + path);

        logger.debug("Path to artifact: " + url);

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN).get();

        return response.getStatus() == 200;
    }

    private void handleFailures(Response response, String message)
            throws ArtifactOperationException
    {
        int status = response.getStatus();
        if (status != 200)
        {
            Object entity = response.getEntity();

            if (entity != null/* && entity instanceof String*/)
            {
//                logger.error((String) entity);
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println(entity);
                System.out.println();
                System.out.println();
                System.out.println();
            }

            // throw new ArtifactOperationException(message);
        }
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
