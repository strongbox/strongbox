package org.carlspring.strongbox.client;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.configuration.ServerConfiguration;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class RestClient
        extends ArtifactClient
{

    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);


    public static RestClient getTestInstance()
    {
        return getTestInstance("maven", "password");
    }

    public static RestClient getTestInstanceLoggedInAsAdmin()
    {
        return getTestInstance("admin", "password");
    }

    public static RestClient getTestInstance(String username,
                                             String password)
    {
        String host = System.getProperty("strongbox.host") != null ?
                      System.getProperty("strongbox.host") :
                      "localhost";

        int port = System.getProperty("strongbox.port") != null ?
                   Integer.parseInt(System.getProperty("strongbox.port")) :
                   48080;

        RestClient client = new RestClient();
        client.setUsername(username);
        client.setPassword(password);
        client.setPort(port);
        client.setContextBaseUrl("http://" + host + ":" + client.getPort());

        return client;
    }

    public int setConfiguration(Configuration configuration)
            throws IOException, JAXBException
    {
        return setServerConfiguration(configuration, "/configuration/strongbox/xml", Configuration.class);
    }

    public Configuration getConfiguration()
            throws IOException, JAXBException
    {
        return (Configuration) getServerConfiguration("/configuration/strongbox/xml", Configuration.class);
    }

    public int setServerConfiguration(ServerConfiguration configuration,
                                      String path,
                                      Class... classes)
            throws IOException, JAXBException
    {
        String url = getContextBaseUrl() + path;

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        GenericParser<ServerConfiguration> parser = new GenericParser<>(classes);
        parser.store(configuration, baos);

        Response response = resource.request(MediaType.TEXT_PLAIN_TYPE)
                                    .put(Entity.entity(baos.toString("UTF-8"), MediaType.APPLICATION_XML));

        return response.getStatus();
    }

    public ServerConfiguration getServerConfiguration(String path,
                                                      Class... classes)
            throws IOException, JAXBException
    {
        String url = getContextBaseUrl() + path;

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        final Response response = resource.request(MediaType.APPLICATION_XML).get();

        ServerConfiguration configuration = null;
        if (response.getStatus() == 200)
        {
            final String xml = response.readEntity(String.class);

            final ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());

            GenericParser<ServerConfiguration> parser = new GenericParser<ServerConfiguration>(classes);

            configuration = parser.parse(bais);
        }

        return configuration;
    }

    /**
     * Sets the listening port.
     *
     * @param port The port to listen on.
     * @return The response from the server.
     */
    public int setListeningPort(int port)
    {
        String url = getContextBaseUrl() + "/configuration/strongbox/port/" + port;

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN).put(Entity.entity(port, MediaType.TEXT_PLAIN));

        return response.getStatus();
    }

    /**
     * Get the port on which the server is listening.
     *
     * @return The port on which the server is listening.
     */
    public int getListeningPort()
    {
        String url = getContextBaseUrl() + "/configuration/strongbox/port";

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        return resource.request(MediaType.TEXT_PLAIN).get(Integer.class);
    }

    /**
     * Sets the base URL of the server.
     *
     * @param baseUrl The base URL.
     * @return The response code.
     */
    public int setBaseUrl(String baseUrl)
    {
        String url = getContextBaseUrl() + "/configuration/strongbox/baseUrl/" + baseUrl;

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN).put(Entity.entity(baseUrl, MediaType.TEXT_PLAIN));

        return response.getStatus();
    }

    /**
     * Gets the base URL of the server.
     *
     * @return The response code.
     */
    public String getBaseUrl()
    {
        String url = getContextBaseUrl() + "/configuration/strongbox/baseUrl";

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        return resource.request(MediaType.TEXT_PLAIN).get(String.class);
    }

    public int setProxyConfiguration(ProxyConfiguration proxyConfiguration)
            throws IOException, JAXBException
    {
        String url = getContextBaseUrl() + "/configuration/strongbox/proxy-configuration";

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        GenericParser<ProxyConfiguration> parser = new GenericParser<ProxyConfiguration>(ProxyConfiguration.class);
        parser.store(proxyConfiguration, baos);

        Response response = resource.request(MediaType.APPLICATION_XML)
                                    .put(Entity.entity(baos.toString("UTF-8"), MediaType.APPLICATION_XML));

        return response.getStatus();
    }

    public ProxyConfiguration getProxyConfiguration(String storageId,
                                                    String repositoryId)
            throws JAXBException
    {
        String url = getContextBaseUrl() + "/configuration/strongbox/proxy-configuration" +
                     (storageId != null && repositoryId != null ?
                      "?storageId=" + storageId + "&repositoryId=" + repositoryId : "");

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        final Response response = resource.request(MediaType.APPLICATION_XML).get();

        @SuppressWarnings("UnnecessaryLocalVariable")
        ProxyConfiguration proxyConfiguration = null;
        if (response.getStatus() == 200)
        {
            final String xml = response.readEntity(String.class);
            final ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());

            GenericParser<ProxyConfiguration> parser = new GenericParser<ProxyConfiguration>(ProxyConfiguration.class);

            proxyConfiguration = parser.parse(bais);
        }
        else
        {
            proxyConfiguration = new ProxyConfiguration();
        }

        return proxyConfiguration;
    }

    /**
     * Creates a new storage.
     *
     * @param storage The storage object to create.
     * @return The response code.
     * @throws IOException
     */
    public int addStorage(Storage storage)
            throws IOException, JAXBException
    {
        String url = getContextBaseUrl() + "/configuration/strongbox/storages";

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        GenericParser<Storage> parser = new GenericParser<Storage>(Storage.class);
        parser.store(storage, baos);

        Response response = resource.request(MediaType.TEXT_PLAIN)
                                    .put(Entity.entity(baos.toString("UTF-8"), MediaType.APPLICATION_XML));

        return response.getStatus();
    }

    /**
     * Looks up a storage by it's ID.
     *
     * @param storageId
     * @return
     * @throws IOException
     */
    public Storage getStorage(String storageId)
            throws IOException, JAXBException
    {
        String url = getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId;

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        final Response response = resource.request(MediaType.APPLICATION_XML).get();

        Storage storage = null;
        if (response.getStatus() == 200)
        {
            final String xml = response.readEntity(String.class);

            final ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());

            GenericParser<Storage> parser = new GenericParser<Storage>(Storage.class);

            storage = parser.parse(bais);
        }

        return storage;
    }

    public static void displayResponseError(Response response)
    {
        logger.error("Status code " + response.getStatus());
        logger.error("Status info " + response.getStatusInfo().getReasonPhrase());
        logger.error("Response message " + response.readEntity(String.class));
        logger.error(response.toString());
    }

    /**
     * Deletes a storage.
     *
     * @param storageId The storage to delete.
     * @return
     */
    public int deleteStorage(String storageId,
                             boolean force)
    {
        String url =
                getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId + (force ? "?force=true" : "");

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request().delete();

        return response.getStatus();
    }

    public int addRepository(Repository repository)
            throws IOException, JAXBException
    {
        if (repository == null)
        {
            logger.error("Unable to add non-existing repository.");
            throw new ServerErrorException("Unable to add non-existing repository.",
                                           Response.Status.INTERNAL_SERVER_ERROR);
        }

        WebTarget resource;

        if (repository.getStorage() == null)
        {
            throw new ServerErrorException("Storage associated with repo is null",
                                           Response.Status.INTERNAL_SERVER_ERROR);
        }

        try
        {
            String url = getContextBaseUrl() + "/configuration/strongbox/storages/" + repository.getStorage().getId();
            resource = getClientInstance().target(url);
        }
        catch (RuntimeException e)
        {
            logger.error("Unable to create web resource.", e);
            throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR);
        }
        setupAuthentication(resource);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        GenericParser<Repository> parser = new GenericParser<Repository>(Repository.class);
        parser.store(repository, baos);

        Response response = resource.request(MediaType.APPLICATION_XML)
                                    .put(Entity.entity(baos.toString("UTF-8"), MediaType.APPLICATION_XML));

        return response.getStatus();
    }

    /**
     * Looks up a repository by it's ID.
     *
     * @param storageId
     * @param repositoryId
     * @return
     * @throws java.io.IOException
     */
    public Repository getRepository(String storageId,
                                    String repositoryId)
            throws IOException, JAXBException
    {
        String url = getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId + "/" + repositoryId;

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        final Response response = resource.request(MediaType.APPLICATION_XML).get();

        Repository repository = null;
        if (response.getStatus() == 200)
        {
            final String xml = response.readEntity(String.class);

            final ByteArrayInputStream bais = new ByteArrayInputStream(xml.getBytes());

            GenericParser<Repository> parser = new GenericParser<Repository>(Repository.class);

            repository = parser.parse(bais);
        }

        return repository;
    }

    /**
     * Deletes a repository.
     *
     * @param storageId    The storage in which the repository to delete is under.
     * @param repositoryId The repository to delete.
     * @return
     */
    public int deleteRepository(String storageId,
                                String repositoryId,
                                boolean force)
    {
        String url = getContextBaseUrl() +
                     "/configuration/strongbox/storages/" + storageId + "/" + repositoryId +
                     (force ? "?force=true" : "");

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request().delete();

        return response.getStatus();
    }

    public String search(String query,
                         MediaType mediaType)
            throws UnsupportedEncodingException
    {
        return search(null, query, mediaType);
    }

    public String search(String repositoryId,
                         String query,
                         MediaType mediaType)
            throws UnsupportedEncodingException
    {
        String url = getContextBaseUrl() + "/search?" +
                     (repositoryId != null ? "repositoryId=" + URLEncoder.encode(repositoryId, "UTF-8") : "") +
                     "&q=" + URLEncoder.encode(query, "UTF-8");

        WebTarget webResource = getClientInstance().target(url);
        setupAuthentication(webResource);

        final Response response = webResource.request(mediaType).get();

        //noinspection UnnecessaryLocalVariable
        final String asText = response.readEntity(String.class);

        return asText;
    }

    public int rebuildMetadata(String storageId,
                               String repositoryId,
                               String basePath)
            throws IOException, JAXBException
    {
        String url = getContextBaseUrl() + "/metadata/" + storageId + "/" + repositoryId + "/" +
                     (basePath != null ? basePath : "");

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN).post(Entity.entity("Rebuild",
                                                                                      MediaType.APPLICATION_XML));

        return response.getStatus();
    }

    public int removeVersionFromMetadata(String storageId,
                                         String repositoryId,
                                         String artifactPath,
                                         String version,
                                         String classifier,
                                         String metadataType)
            throws IOException, JAXBException
    {
        String url = getContextBaseUrl() + "/metadata/" +
                     storageId + "/" + repositoryId + "/" +
                     (artifactPath != null ? artifactPath : "") +
                     "?version=" + version + (classifier != null ? "&classifier=" + classifier : "") +
                     "&metadataType=" + metadataType;

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request().delete();

        return response.getStatus();
    }

    public void copy(String path,
                     String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId)
    {
        @SuppressWarnings("ConstantConditions")
        String url = getContextBaseUrl() + "/storages/copy/" + path +
                     "?srcStorageId=" + srcStorageId +
                     "&srcRepositoryId=" + srcRepositoryId +
                     "&destStorageId=" + destStorageId +
                     "&destRepositoryId=" + destRepositoryId;

        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        resource.request(MediaType.TEXT_PLAIN).post(Entity.entity("Copy", MediaType.TEXT_PLAIN));
    }

    public String greet()
    {
        String url = getContextBaseUrl() + "/storages/greet";
        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);

        Response response = resource.request(MediaType.TEXT_PLAIN).get();
        if (response.getStatus() != 200)
        {
            displayResponseError(response);
            throw new ServerErrorException(response.getStatus() + " | Unable to greet()", Response.Status.INTERNAL_SERVER_ERROR);
        }
        else
        {
            return response.getEntity().toString();
        }
    }

    public WebTarget prepareTarget(String arg){
        String url = getContextBaseUrl() + arg;
        logger.debug("Prepare target URL " + url);
        WebTarget resource = getClientInstance().target(url);
        setupAuthentication(resource);
        return resource;
    }
}
