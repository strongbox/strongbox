package org.carlspring.strongbox.client;

import org.carlspring.maven.commons.util.ArtifactUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class ArtifactResolver
        implements Closeable
{

    private static final Logger logger = LoggerFactory.getLogger(ArtifactResolver.class);

    private String repositoryBaseUrl;

    private String username;

    private String password;

    private Client client;


    public ArtifactResolver(Client client)
    {
        this.client = client;
    }

    public static ArtifactResolver getTestInstance(Client client,
                                                   String repositoryBaseUrl,
                                                   String username,
                                                   String password)
    {
        ArtifactResolver resolver = new ArtifactResolver(client);
        resolver.setUsername(username);
        resolver.setPassword(password);
        resolver.setRepositoryBaseUrl(repositoryBaseUrl);

        return resolver;
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

        WebTarget resource = new WebTargetBuilder(url)
                                     .withAuthentication()
                                     .customRequestConfig()
                                     .build();

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

    public Response getResourceWithResponse(String path)
            throws ArtifactTransportException,
                   IOException
    {
        String url = escapeUrl(path);

        logger.debug("Getting " + url + "...");

        WebTarget resource = new WebTargetBuilder(url)
                                     .withAuthentication()
                                     .customRequestConfig()
                                     .build();

        return resource.request().get();
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

    public Response artifactExistsStatusCode(Artifact artifact,
                                             String storageId,
                                             String repositoryId)
            throws ResponseException
    {
        String url = getUrlForArtifact(artifact, storageId, repositoryId);

        logger.debug("Path to artifact: " + url);

        WebTarget resource = new WebTargetBuilder(url)
                                     .withAuthentication()
                                     .customRequestConfig()
                                     .build();

        return resource.request(MediaType.TEXT_PLAIN).header("user-agent", "Maven/*").get();
    }

    public boolean pathExists(String path)
    {
        String url = escapeUrl(path);

        logger.debug("Path to artifact: " + url);

        WebTarget resource = new WebTargetBuilder(url)
                                     .withAuthentication()
                                     .customRequestConfig()
                                     .build();

        Response response = resource.request(MediaType.TEXT_PLAIN).header("user-agent", "Maven/*").get();
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

    public Metadata retrieveMetadata(String path)
            throws ArtifactTransportException,
                   IOException,
                   XmlPullParserException
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

    private class WebTargetBuilder
    {

        private final WebTarget target;

        private WebTargetBuilder(String uri)
        {
            this.target = client.target(uri);
        }

        private WebTargetBuilder withAuthentication()
        {
            if (username != null && password != null)
            {
                target.register(HttpAuthenticationFeature.basic(username, password));
            }
            return this;
        }

        private WebTargetBuilder customRequestConfig()
        {
            target.property(ApacheClientProperties.REQUEST_CONFIG,
                            RequestConfig.custom().setCircularRedirectsAllowed(true).build());
            return this;
        }

        public WebTarget build()
        {
            return target;
        }
    }

}
