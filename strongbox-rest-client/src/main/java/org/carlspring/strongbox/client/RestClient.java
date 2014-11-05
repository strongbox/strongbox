package org.carlspring.strongbox.client;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mtodorov
 */
public class RestClient extends ArtifactClient
{

    private static final Logger logger = LoggerFactory.getLogger(RestClient.class);


    /**
     * Sets the listening port.
     *
     * @param port  The port to listen on.
     * @return      The response from the server.
     */
    public int setListeningPort(int port)
    {
        Client client = ClientBuilder.newClient();
        int newPort = 18080;

        WebTarget resource = client.target(getContextBaseUrl() + "/configuration/strongbox/port/" + newPort);

        Response response = resource.request(MediaType.TEXT_PLAIN)
                                    .put(Entity.entity(newPort, MediaType.TEXT_PLAIN));

        return response.getStatus();
    }

    /**
     * Get the port on which the server is listening.
     * @return      The port on which the server is listening.
     */
    public int getPort()
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

        WebTarget resource = client.target(getContextBaseUrl() + "/configuration/strongbox/storage/" + storage.getId());

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

        WebTarget resource = client.target(getContextBaseUrl() + "/configuration/strongbox/storage/" + storageId);

        final String xml = resource.request(MediaType.APPLICATION_XML).get(String.class);

        final ByteArrayInputStream baos = new ByteArrayInputStream(xml.getBytes());

        System.out.println(xml);

        JAXBContext context = JAXBContext.newInstance(Storage.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        @SuppressWarnings("UnnecessaryLocalVariable")
        Storage storage = (Storage) unmarshaller.unmarshal(baos);

        return storage;
    }

    /**
     * Creates a new repository.
     *
     * @param storageId     The ID of the storage object used to associate the repository with.
     * @param repository    The repository object to create.
     * @return              The response code.
     * @throws IOException
     */
    public int addRepository(String storageId, Repository repository)
            throws IOException, JAXBException
    {
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() + "/configuration/strongbox/repository/" + storageId + "/" + repository.getId() +  "?" +
                                           (repository.getPolicy() != null ? "policy=" + repository.getPolicy() : "") +
                                           (repository.getImplementation() != null ? "&implementation=" + repository.getImplementation() : "") +
                                           (repository.getType() != null ? "&type=" + repository.getType() : "") +
                                           "&secured=" + repository.isSecured() +
                                           "&trashEnabled=" + repository.isTrashEnabled() +
                                           "&allowsForceDeletion=" + repository.allowsForceDeletion() +
                                           "&allowsRedeployment=" + repository.allowsRedeployment());

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
            throws java.io.IOException, JAXBException
    {
        Client client = ClientBuilder.newClient();

        WebTarget resource = client.target(getContextBaseUrl() +
                                           "/configuration/strongbox/repository/" +
                                           storageId + "/" + repositoryId);

        final String xml = resource.request(MediaType.APPLICATION_XML).get(String.class);

        final ByteArrayInputStream baos = new ByteArrayInputStream(xml.getBytes());

        JAXBContext context = JAXBContext.newInstance(Repository.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        @SuppressWarnings("UnnecessaryLocalVariable")
        Repository repository = (Repository) unmarshaller.unmarshal(baos);

        return repository;
    }

    public String search(String query, MediaType mediaType)
            throws UnsupportedEncodingException
    {
        return search(null, query, mediaType);
    }

    public String search(String repository, String query, MediaType mediaType)
            throws UnsupportedEncodingException
    {
        Client client = ClientBuilder.newClient();

        String url = getContextBaseUrl() + "/search?" +
                     (repository != null ? "repository=" + URLEncoder.encode(repository, "UTF-8") : "") +
                     "&q=" + URLEncoder.encode(query, "UTF-8");

        WebTarget webResource = client.target(url);
        setupAuthentication(webResource);

        final Response response = webResource.request(mediaType).get();

        final String asText = response.readEntity(String.class);

        logger.info(asText);

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
