package org.carlspring.strongbox.client;

import org.carlspring.maven.commons.util.ArtifactUtils;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;

import com.sun.jersey.api.client.*;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.apache.maven.artifact.Artifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class ArtifactClient
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactClient.class);

    public static final String MANAGEMENT_URL = "manage/artifact";

    private String host = "http://localhost";

    private String contextBaseUrl;

    private int port = 48080;

    private String username;

    private String password;


    public void addArtifact(Artifact artifact,
                            String repository,
                            long length)
            throws ArtifactOperationException
    {
        Client client = Client.create();

        setupAuthentication(client);

        String url = host + ":" + port + "/" + MANAGEMENT_URL + "/" +
                     repository + "/state/EXISTS/length/" + length + "/" +
                     ArtifactUtils.convertArtifactToPath(artifact);

        logger.debug("Using " + url);

        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.type(MediaType.TEXT_PLAIN).post(ClientResponse.class);

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
        Client client = Client.create();

        setupAuthentication(client);

        String url = host + ":" + port + "/" + contextBaseUrl + "/" +
                     repository + "/" +
                     ArtifactUtils.convertArtifactToPath(artifact);

        logger.debug("Getting " + url + "...");

        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.get(ClientResponse.class);

        final InputStream is = response.getEntity(InputStream.class);

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
                               String storage,
                               String repository)
    {
        Client client = Client.create();

        setupAuthentication(client);

        String url = getUrlForArtifact(artifact, storage, repository);

        WebResource webResource = client.resource(url);
        webResource.delete();
    }

    public void delete(String storage,
                       String repository,
                       String path)
    {
        Client client = Client.create();

        setupAuthentication(client);

        String url = host + ":" + port + "/storages/" + storage + "/" + repository + "/" + path;

        WebResource webResource = client.resource(url);
        webResource.delete();
    }

    public boolean artifactExists(Artifact artifact,
                                  String storage,
                                  String repository)
            throws ResponseException
    {
        ClientResponse response = artifactExistsStatusCode(artifact, storage, repository);

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

    public ClientResponse artifactExistsStatusCode(Artifact artifact,
                                                   String storage,
                                                   String repository)
            throws ResponseException
    {
        Client client = Client.create();

        setupAuthentication(client);

        String url = getUrlForArtifact(artifact, storage, repository);

        logger.debug("Path to artifact: " + url);

        WebResource webResource = client.resource(url);
        ClientResponse response = null;
        try
        {
            response = webResource.accept("application/xml").get(ClientResponse.class);
        }
        catch (UniformInterfaceException e)
        {
            //noinspection ConstantConditions
            throw new ResponseException(e.getMessage(), response != null ? response.getStatus() : 0);
        }
        catch (ClientHandlerException e)
        {
            //noinspection ConstantConditions
            throw new ResponseException(e.getMessage(), response != null ? response.getStatus() : 0);
        }

        return response;
    }

    public boolean pathExists(String path)
    {
        Client client = Client.create();

        setupAuthentication(client);

        String url = host + ":" + port + (path.startsWith("/") ? path : '/' + path);

        logger.debug("Path to artifact: " + url);

        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

        return response.getStatus() == 200;
    }

    public String getUrlForArtifact(Artifact artifact,
                                    String storage,
                                    String repository)
    {
        return host + ":" + port + "/storages/" + storage + "/" +
               repository + "/" +
               ArtifactUtils.convertArtifactToPath(artifact);
    }

    private void setupAuthentication(Client client)
    {
        if (username != null && password != null)
        {
            client.addFilter(new HTTPBasicAuthFilter(username, password));
        }
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
