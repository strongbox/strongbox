package org.carlspring.repositoryunit.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.maven.artifact.Artifact;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;

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


    public void addArtifact(Artifact artifact,
                            String repository,
                            long length)
            throws ArtifactOperationException
    {
        Client client = Client.create();

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

        String url = host + ":" + port + "/" + contextBaseUrl + "/" +
                     repository + "/" +
                     ArtifactUtils.convertArtifactToPath(artifact);

        logger.debug("Getting " + url + "...");

        WebResource webResource = client.resource(url);
        ClientResponse response = webResource.get(ClientResponse.class);

        final InputStream is = response.getEntity(InputStream.class);

        int total = 0;
        int len;
        int size = 4096;
        byte[] bytes = new byte[size];

        while ((len = is.read(bytes, 0, size)) != -1)
        {
            total += len;
        }

        logger.debug("Response code: " + response.getStatus() +". Read: " + total + " bytes.");

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
                               String repository)
    {
        Client client = Client.create();

        String url = host + ":" + port + "/" + MANAGEMENT_URL + "/" +
                     repository + "/" +
                     ArtifactUtils.convertArtifactToPath(artifact);

        WebResource webResource = client.resource(url);
        webResource.type(MediaType.TEXT_PLAIN).delete();
    }

    public boolean artifactExists(String repoUrl, Artifact artifact)
    {
        Client client = Client.create();
        final String pathToArtifact = repoUrl + ArtifactUtils.convertArtifactToPath(artifact);

        logger.debug("Path to artifact: " + pathToArtifact);

        WebResource webResource = client.resource(pathToArtifact);
        ClientResponse response = webResource.accept("application/xml").get(ClientResponse.class);

        return response.getStatus() == 200;
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

}
