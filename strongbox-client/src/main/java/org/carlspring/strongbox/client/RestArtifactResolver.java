package org.carlspring.strongbox.client;

import org.carlspring.maven.commons.util.ArtifactUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Feature;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class RestArtifactResolver
        implements Closeable
{

    private static final Logger logger = LoggerFactory.getLogger(RestArtifactResolver.class);

    private final String repositoryBaseUrl;
    private final Client client;
    private Feature authentication;


    public RestArtifactResolver(Client client,
                                String repositoryBaseUrl)
    {
        this.client = client;
        this.repositoryBaseUrl = normalize(repositoryBaseUrl);
    }

    public RestArtifactResolver(Client client,
                                String repositoryBaseUrl,
                                Feature authentication)
    {
        this(client, repositoryBaseUrl);
        this.authentication = authentication;
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

    public InputStream getResource(String path,
                                   long offset)
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

    public Response head(String path)
            throws ArtifactTransportException,
                   IOException
    {
        String url = escapeUrl(path);

        logger.debug("Heading " + url + "...");

        WebTarget resource = new WebTargetBuilder(url)
                                     .withAuthentication()
                                     .customRequestConfig()
                                     .build();

        return resource.request().head();
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

    private String normalize(String repositoryBaseUrl)
    {
        return repositoryBaseUrl.endsWith("/") ? repositoryBaseUrl : repositoryBaseUrl + "/";
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
            if (authentication != null)
            {
                target.register(authentication);
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
