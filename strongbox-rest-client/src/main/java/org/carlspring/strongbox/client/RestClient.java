package org.carlspring.strongbox.client;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.configuration.ServerConfiguration;
import org.carlspring.strongbox.rest.ObjectMapperProvider;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class RestClient extends ArtifactClient
{

    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);


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

    public int setServerConfiguration(ServerConfiguration configuration, String path, Class... clazz)
            throws IOException, JAXBException
    {
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() + path);

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        JAXBContext context = JAXBContext.newInstance(clazz);

        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(configuration, baos);

        Response response = resource.request(MediaType.APPLICATION_XML)
                                    .put(Entity.entity(baos.toString("UTF-8"), MediaType.APPLICATION_XML));

        return response.getStatus();
    }

    public ServerConfiguration getServerConfiguration(String path, Class... clazz)
            throws IOException, JAXBException
    {
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() + path);

        final Response response = resource.request(MediaType.APPLICATION_XML).get();

        ServerConfiguration configuration = null;
        if (response.getStatus() == 200)
        {
            final String xml = response.readEntity(String.class);

            final ByteArrayInputStream baos = new ByteArrayInputStream(xml.getBytes());

            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            configuration = (ServerConfiguration) unmarshaller.unmarshal(baos);
        }

        return configuration;
    }

    /**
     * Sets the listening port.
     *
     * @param port  The port to listen on.
     * @return      The response from the server.
     */
    public int setListeningPort(int port)
    {
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() + "/configuration/strongbox/port/" + port);

        Response response = resource.request(MediaType.TEXT_PLAIN)
                                    .put(Entity.entity(port, MediaType.TEXT_PLAIN));

        return response.getStatus();
    }

    /**
     * Get the port on which the server is listening.
     * @return      The port on which the server is listening.
     */
    public int getListeningPort()
    {
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() + "/configuration/strongbox/port");

        return resource.request(MediaType.TEXT_PLAIN).get(Integer.class);
    }

    /**
     * Sets the base URL of the server.
     *
     * @param baseUrl   The base URL.
     * @return          The response code.
     */
    public int setBaseUrl(String baseUrl)
    {
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() + "/configuration/strongbox/baseUrl/" + baseUrl);

        Response response = resource.request(MediaType.TEXT_PLAIN).put(Entity.entity(baseUrl, MediaType.TEXT_PLAIN));

        return response.getStatus();
    }

    /**
     * Gets the base URL of the server.
     *
     * @return          The response code.
     */
    public String getBaseUrl()
    {
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() + "/configuration/strongbox/baseUrl");

        return resource.request(MediaType.TEXT_PLAIN).get(String.class);
    }

    public int setProxyConfiguration(ProxyConfiguration proxyConfiguration)
            throws IOException, JAXBException
    {
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() + "/configuration/strongbox/proxy-configuration");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        JAXBContext context = JAXBContext.newInstance(ProxyConfiguration.class);

        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(proxyConfiguration, baos);

        Response response = resource.request(MediaType.APPLICATION_XML)
                                    .put(Entity.entity(baos.toString("UTF-8"), MediaType.APPLICATION_XML));

        return response.getStatus();
    }

    public ProxyConfiguration getProxyConfiguration(String storageId, String repositoryId)
            throws JAXBException
    {
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() + "/configuration/strongbox/proxy-configuration" +
                                           (storageId != null && repositoryId != null ?
                                            "?storageId=" + storageId + "&repositoryId=" + repositoryId : ""));

        final Response response = resource.request(MediaType.APPLICATION_XML).get();

        @SuppressWarnings("UnnecessaryLocalVariable")
        ProxyConfiguration proxyConfiguration = null;
        if (response.getStatus() == 200)
        {
            final String xml = response.readEntity(String.class);
            final ByteArrayInputStream baos = new ByteArrayInputStream(xml.getBytes());

            JAXBContext context = JAXBContext.newInstance(ProxyConfiguration.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            proxyConfiguration = (ProxyConfiguration) unmarshaller.unmarshal(baos);
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
     * @param storage   The storage object to create.
     * @return          The response code.
     * @throws IOException
     */
    public int addStorage(Storage storage)
            throws IOException, JAXBException
    {
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() + "/configuration/strongbox/storages");

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        JAXBContext context = JAXBContext.newInstance(Storage.class);

        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(storage, baos);

        Response response = resource.request(MediaType.APPLICATION_XML)
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
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId);

        final Response response = resource.request(MediaType.APPLICATION_XML).get();

        Storage storage = null;
        if (response.getStatus() == 200)
        {
            final String xml = response.readEntity(String.class);

            final ByteArrayInputStream baos = new ByteArrayInputStream(xml.getBytes());

            JAXBContext context = JAXBContext.newInstance(Storage.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            storage = (Storage) unmarshaller.unmarshal(baos);
        }

        return storage;
    }

    /**
     * Deletes a storage.
     *
     * @param storageId     The storage to delete.
     * @return
     */
    public int deleteStorage(String storageId)
    {
        Client client = ClientBuilder.newClient();

        String url = getHost() + ":" + getPort() + "/configuration/strongbox/storages/" + storageId;

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);

        Response response = webResource.request().delete();

        return response.getStatus();
    }

    public int addRepository(Repository repository)
            throws IOException, JAXBException
    {
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() +
                                           "/configuration/strongbox/storages/" +
                                           repository.getStorage().getId());

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        JAXBContext context = JAXBContext.newInstance(Repository.class);

        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.marshal(repository, baos);

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
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() +
                                           "/configuration/strongbox/storages/" +
                                           storageId + "/" + repositoryId);

        final Response response = resource.request(MediaType.APPLICATION_XML).get();

        Repository repository = null;
        if (response.getStatus() == 200)
        {
            final String xml = response.readEntity(String.class);

            final ByteArrayInputStream baos = new ByteArrayInputStream(xml.getBytes());

            JAXBContext context = JAXBContext.newInstance(Repository.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();

            repository = (Repository) unmarshaller.unmarshal(baos);
        }

        return repository;
    }

    /**
     * Deletes a repository.
     *
     * @param storageId         The storage in which the repository to delete is under.
     * @param repositoryId      The repository to delete.
     * @return
     */
    public int deleteRepository(String storageId,
                                String repositoryId,
                                boolean deleteFromFileSystem)
    {
        Client client = ClientBuilder.newClient();

        String url = getHost() + ":" + getPort() +
                     "/configuration/strongbox/storages/" + storageId + "/" + repositoryId +
                     (deleteFromFileSystem ? "?deleteFromFileSystem=true" : "");

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);

        Response response = webResource.request().delete();

        return response.getStatus();
    }

    public String search(String query, MediaType mediaType)
            throws UnsupportedEncodingException
    {
        return search(null, query, mediaType);
    }

    public String search(String repository, String query, MediaType mediaType)
            throws UnsupportedEncodingException
    {
        final Client client = ClientBuilder.newBuilder()
                                           .register(ObjectMapperProvider.class)
                                           .register(JacksonFeature.class)
                                           .build();

        String url = getContextBaseUrl() + "/search?" +
                     (repository != null ? "repository=" + URLEncoder.encode(repository, "UTF-8") : "") +
                     "&q=" + URLEncoder.encode(query, "UTF-8");

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);

        final Response response = webResource.request(mediaType).get();

        //noinspection UnnecessaryLocalVariable
        final String asText = response.readEntity(String.class);

        return asText;
    }

    public void deleteTrash(String storage, String repository)
    {
        Client client = ClientBuilder.newClient();

        String url = getUrlForTrash(storage, repository);

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);
        webResource.request().delete();
    }

    public void deleteTrash()
    {
        Client client = ClientBuilder.newClient();

        String url = getHost() + ":" + getPort() + "/trash";

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);
        webResource.request().delete();
    }

    public String getUrlForTrash(String storage, String repository)
    {
        return getHost() + ":" + getPort() + "/trash/" + storage + "/" + repository;
    }

}
