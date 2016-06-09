package org.carlspring.strongbox.client;

import org.apache.http.HttpStatus;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.carlspring.maven.commons.util.ArtifactUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author mtodorov
 */
public class MavenArtifactClient implements Closeable
{

    private static final Logger logger = LoggerFactory.getLogger(MavenArtifactClient.class);

    private String repositoryBaseUrl;

    private String username;

    private String password;

    private Client client;

    public MavenArtifactClient(Client client)
    {
        this.client = client;
    }

    public static MavenArtifactClient getTestInstance(Client client, String repositoryBaseUrl,
                                                      String username,
                                                      String password)
    {
        MavenArtifactClient mavenArtifactClient = new MavenArtifactClient(client);
        mavenArtifactClient.setUsername(username);
        mavenArtifactClient.setPassword(password);
        mavenArtifactClient.setRepositoryBaseUrl(repositoryBaseUrl);

        return mavenArtifactClient;
    }

    public Client getClientInstance()
    {
        return client;
    }

    @Override
    public void close()
    {
        if (client != null)
        {
            client.close();
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
        String url = escapeUrl(path);

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

        try
        {
            if (response.getStatus() != HttpStatus.SC_OK || response.getEntity() == null)
            {
                return null;
            }
            else
            {
                return response.readEntity(InputStream.class);
            }
        }
        finally
        {
            response.close();
        }
    }

    public boolean artifactExists(Artifact artifact,
                                  String storageId,
                                  String repositoryId)
            throws ResponseException
    {
        Response response = artifactExistsStatusCode(artifact, storageId, repositoryId);

        try
        {
            if (response.getStatus() == HttpStatus.SC_OK)
            {
                return true;
            }
            else if (response.getStatus() == HttpStatus.SC_NOT_FOUND)
            {
                return false;
            }
            else
            {
                throw new ResponseException(response.getStatusInfo().getReasonPhrase(), response.getStatus());
            }
        }
        finally
        {
            response.close();
        }
    }

    private Response artifactExistsStatusCode(Artifact artifact, String storageId, String repositoryId)
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
        String url = escapeUrl(path);

        logger.debug("Path to artifact: " + url);

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN).get();
        try
        {
            return response.getStatus() == HttpStatus.SC_OK;
        }
        finally
        {
            response.close();
        }
    }

    private String escapeUrl(String path)
    {
        String baseUrl = getRepositoryBaseUrl() + (getRepositoryBaseUrl().endsWith("/") ? "" : "/");
        String p = (path.startsWith("/") ? path.substring(1, path.length()) : path);

        return baseUrl + p;
    }

    public String getUrlForArtifact(Artifact artifact,
                                    String storageId,
                                    String repositoryId)
    {
        return getRepositoryBaseUrl() +
               "storages/" + storageId + "/" + repositoryId + "/" +
               ArtifactUtils.convertArtifactToPath(artifact);
    }

    public void setupAuthentication(WebTarget target)
    {
        if (username != null && password != null)
        {
            target.register(HttpAuthenticationFeature.basic(username, password));
        }
    }

    public Metadata retrieveMetadata(String path) throws ArtifactTransportException, IOException, XmlPullParserException
    {
        if (pathExists(path))
        {
            InputStream is = getResource(path);
            try
            {
                MetadataXpp3Reader reader = new MetadataXpp3Reader();
                return reader.read(is);
            }
            finally
            {
                is.close();
            }
        }
        return null;
    }

    public String getRepositoryBaseUrl()
    {
        return repositoryBaseUrl;
    }

    public void setRepositoryBaseUrl(String repositoryBaseUrl)
    {
        if (repositoryBaseUrl.endsWith("/"))
        {
            this.repositoryBaseUrl = repositoryBaseUrl;
        }
        else
        {
            this.repositoryBaseUrl = repositoryBaseUrl + "/";
        }
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
