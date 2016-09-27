package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.client.ArtifactSpringClient;
import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.configuration.ServerConfiguration;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.xml.parsers.GenericParser;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yury on 9/14/16.
 */
public class SpringClient
        extends ArtifactSpringClient {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpringClient.class);
    private RestTemplate restTemplate = new RestTemplate();


    public static SpringClient getTestInstance() {
        return getTestInstance("maven", "password");
    }

    public static SpringClient getTestInstanceLoggedInAsAdmin() {
        return getTestInstance("admin", "password");
    }

    public static SpringClient getTestInstance(String username,
                                               String password) {
        String host = System.getProperty("strongbox.host") != null ?
                System.getProperty("strongbox.host") :
                "localhost";

        int port = System.getProperty("strongbox.port") != null ?
                Integer.parseInt(System.getProperty("strongbox.port")) :
                48080;

        SpringClient client = new SpringClient();
        client.setUsername(username);
        client.setPassword(password);
        client.setPort(port);
        client.setContextBaseUrl("http://" + host + ":" + client.getPort());

        return client;
    }

    public static void displayResponseError(ResponseEntity response) {
        logger.error("Status code " + response.getStatusCode().value());
        logger.error("Status info " + response.getStatusCode().getReasonPhrase());
        logger.error("Response message " + response.getBody().toString());
        logger.error(response.toString());
    }

    public int setConfiguration(Configuration configuration)
            throws IOException, JAXBException {
        return setServerConfiguration(configuration, "/configuration/strongbox/xml", Configuration.class);
    }

    public Configuration getConfiguration()
            throws IOException, JAXBException {
        return (Configuration) getServerConfiguration("/configuration/strongbox/xml", Configuration.class);
    }

    public int setServerConfiguration(ServerConfiguration configuration,
                                      String path,
                                      Class... classes)
            throws IOException, JAXBException {
        String url = getContextBaseUrl() + path;

        GenericParser<ServerConfiguration> parser = new GenericParser<>(classes);
        String serializedConfiguration = parser.serialize(configuration);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        HttpEntity<String> entity = new HttpEntity<String>(serializedConfiguration, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        return response.getStatusCode().value();
    }

    public ServerConfiguration getServerConfiguration(String path,
                                                      Class... classes)
            throws IOException, JAXBException {
        String url = getContextBaseUrl() + path;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        ServerConfiguration configuration = null;
        if (response.getStatusCode().value() == 200) {
            final String xml = response.getBody().toString();

            GenericParser<ServerConfiguration> parser = new GenericParser<>(classes);
            configuration = parser.deserialize(xml);
        }

        return configuration;
    }

    /**
     * Sets the listening port.
     *
     * @param port The port to listen on.
     * @return The response from the server.
     */
    public int setListeningPort(int port) {
        String url = getContextBaseUrl() + "/configuration/strongbox/port/" + port;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<Integer> entity = new HttpEntity<Integer>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class, port);

        return response.getStatusCode().value();
    }

    /**
     * Get the port on which the server is listening.
     *
     * @return The port on which the server is listening.
     */
    public int getListeningPort() {
        String url = getContextBaseUrl() + "/configuration/strongbox/port";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        return Integer.valueOf(response.getBody());
    }

    /**
     * Sets the base URL of the server.
     *
     * @param baseUrl The base URL.
     * @return The response code.
     */
    public int setBaseUrl(String baseUrl) {
        String url = getContextBaseUrl() + "/configuration/strongbox/baseUrl";

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>(baseUrl, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        return response.getStatusCode().value();
    }

    /**
     * Gets the base URL of the server.
     *
     * @return The response code.
     */
    public String getBaseUrl() {
        String url = getContextBaseUrl() + "/configuration/strongbox/baseUrl";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }

    public int setProxyConfiguration(ProxyConfiguration proxyConfiguration)
            throws IOException, JAXBException {
        String url = getContextBaseUrl() + "/configuration/strongbox/proxy-configuration";

        GenericParser<ProxyConfiguration> parser = new GenericParser<>(ProxyConfiguration.class);
        String serializedConfig = parser.serialize(proxyConfiguration);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(serializedConfig, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        return response.getStatusCode().value();
    }

    public ProxyConfiguration getProxyConfiguration(String storageId,
                                                    String repositoryId)
            throws JAXBException {
        String url = getContextBaseUrl() + "/configuration/strongbox/proxy-configuration";

        storageId = (storageId != null ? storageId : " ");
        repositoryId = (repositoryId != null ? repositoryId : " ");

        System.out.println("storageID " + storageId);
        System.out.println("repositoryID " + repositoryId);
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class, storageId, repositoryId);

        @SuppressWarnings("UnnecessaryLocalVariable")
        ProxyConfiguration proxyConfiguration;
        if (response.getStatusCode().value() == 200) {
            final String xml = response.getBody();

            GenericParser<ProxyConfiguration> parser = new GenericParser<>(ProxyConfiguration.class);
            proxyConfiguration = parser.deserialize(xml);
        } else {
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
            throws IOException, JAXBException {
        String url = getContextBaseUrl() + "/configuration/strongbox/storages";

        GenericParser<Storage> parser = new GenericParser<>(Storage.class);
        String serializedStorage = parser.serialize(storage);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(serializedStorage, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        return response.getStatusCode().value();
    }

    /**
     * Looks up a storage by it's ID.
     *
     * @param storageId
     * @return
     * @throws IOException
     */
    public Storage getStorage(String storageId)
            throws IOException, JAXBException {
        String url = getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        Storage storage = null;
        if (response.getStatusCode().value() == 200) {
            final String xml = response.getBody();

            GenericParser<Storage> parser2 = new GenericParser<>(Storage.class);
            storage = parser2.deserialize(xml);
        }

        return storage;
    }

    /**
     * Deletes a storage.
     *
     * @param storageId The storage to delete.
     * @return
     */
    public int deleteStorage(String storageId,
                             boolean force) {
        String url =
                getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        Map<String, Boolean> param = new HashMap<String, Boolean>();
        param.put("force", force);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class, param);

        return response.getStatusCode().value();
    }

    public int addRepository(Repository repository)
            throws IOException, JAXBException {
        String url;
        if (repository == null) {
            logger.error("Unable to add non-existing repository.");
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to add non-existing repository.");
        }


        if (repository.getStorage() == null) {
            logger.error("Storage associated with repo is null.");
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Storage associated with repo is null.");
        }

        try {
            url = getContextBaseUrl() + "/configuration/strongbox/storages/" + repository.getStorage().getId() + "/" + repository.getId();

        } catch (RuntimeException e) {
            logger.error("Unable to create web resource.", e);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        GenericParser<Repository> parser = new GenericParser<>(Repository.class);
        String serializedRepository = parser.serialize(repository);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(serializedRepository, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);

        return response.getStatusCode().value();
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
            throws IOException, JAXBException {
        String url = getContextBaseUrl() + "/configuration/strongbox/storages/" + storageId + "/" + repositoryId;

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        Repository repository = null;
        if (response.getStatusCode().value() == 200) {
            final String xml = response.getBody();

            GenericParser<Repository> parser2 = new GenericParser<>(Repository.class);
            repository = parser2.deserialize(xml);
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
                                boolean force) {
        String url = getContextBaseUrl() +
                "/configuration/strongbox/storages/" + storageId + "/" + repositoryId;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class, force);

        return response.getStatusCode().value();
    }

    public String search(String query,
                         MediaType mediaType)
            throws UnsupportedEncodingException {
        return search(null, query, mediaType);
    }

    public String search(String repositoryId,
                         String query,
                         MediaType mediaType)
            throws UnsupportedEncodingException {
        String url = getContextBaseUrl() + "/search";

             /*   (repositoryId != null ? "repositoryId=" + URLEncoder.encode(repositoryId, "UTF-8") : "") +
                "&q=" + URLEncoder.encode(query, "UTF-8");*/


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class, URLEncoder.encode(repositoryId, "UTF-8"), URLEncoder.encode(query, "UTF-8"));

        //noinspection UnnecessaryLocalVariable
        final String asText = response.getBody();

        return asText;
    }

    public int rebuildMetadata(String storageId,
                               String repositoryId,
                               String basePath)
            throws IOException, JAXBException {
        String url = getContextBaseUrl() + "/metadata/" + storageId + "/" + repositoryId + "/" +
                (basePath != null ? basePath : "");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        return response.getStatusCode().value();
    }

    public int removeVersionFromMetadata(String storageId,
                                         String repositoryId,
                                         String artifactPath,
                                         String version,
                                         String classifier,
                                         String metadataType)
            throws IOException, JAXBException {
        String url = getContextBaseUrl() + "/metadata/" +
                storageId + "/" + repositoryId + "/" +
                (artifactPath != null ? artifactPath : "") +
                "?version=" + version + (classifier != null ? "&classifier=" + classifier : "") +
                "&metadataType=" + metadataType;

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, null, String.class);

        return response.getStatusCode().value();
    }

    public void copy(String path,
                     String srcStorageId,
                     String srcRepositoryId,
                     String destStorageId,
                     String destRepositoryId) {
        @SuppressWarnings("ConstantConditions")
        String url = getContextBaseUrl() + "/storages/copy/" + path +
                "?srcStorageId=" + srcStorageId +
                "&srcRepositoryId=" + srcRepositoryId +
                "&destStorageId=" + destStorageId +
                "&destRepositoryId=" + destRepositoryId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
    }

    public String greet() {
        String url = getContextBaseUrl() + "/storages/greet";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<String>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode().value() != 200) {
            displayResponseError(response);
            throw new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode() + " | Unable to greet()");
        } else {
            return response.getBody().toString();
        }
    }

    public void resetAuthentication() {
        this.username = "admin";
        this.password = "password";
    }
}
