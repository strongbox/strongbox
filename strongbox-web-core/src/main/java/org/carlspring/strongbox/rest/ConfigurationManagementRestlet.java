package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.security.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.IOException;

import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
@Path("/configuration/strongbox")
public class ConfigurationManagementRestlet
        extends BaseRestlet
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementRestlet.class);

    @Autowired
    private ConfigurationManagementService configurationManagementService;


    // TODO: 1) Implement XML config upload    /xml/add
    // TODO: 2) Implement XML config download  /xml/get


    @PUT
    @Path("/baseUrl/{baseUrl:.*}")
    public Response setBaseUrl(@PathParam("baseUrl") String baseUrl)
            throws IOException,
                   AuthenticationException,
                   JAXBException
    {
        configurationManagementService.setBaseUrl(baseUrl);

        logger.info("Set baseUrl to " + baseUrl);

        return Response.ok().build();
    }

    @GET
    @Path("/baseUrl")
    @Produces(MediaType.TEXT_PLAIN)
    public String getBaseUrl()
            throws IOException,
                   AuthenticationException
    {
        return configurationManagementService.getBaseUrl();
    }

    @PUT
    @Path("/port/{port}")
    public Response setPort(@PathParam("port") int port)
            throws IOException, JAXBException
    {
        configurationManagementService.setPort(port);

        logger.info("Set port to " + port + ". This operation will require a server restart.");

        return Response.ok().build();
    }

    @GET
    @Path("/port")
    @Produces(MediaType.TEXT_PLAIN)
    public int getPort()
            throws IOException,
                   AuthenticationException
    {
        return configurationManagementService.getPort();
    }

    /**
     * Creates a storage.
     *
     * NOTE: This method ONLY creates the storage. Adding repositories should be done as a separate step.
     *
     * @param storageId         The ID of the storage.
     * @param basedir           The base directory of the storage.
     * @return                  The response code
     * @throws IOException
     */
    @PUT
    @Path("/storage/{storageId}")
    public Response addOrUpdateStorage(@PathParam("storageId") String storageId,
                                       @QueryParam("basedir") String basedir)
            throws IOException, JAXBException
    {
        Storage storage = new Storage(storageId, basedir);

        configurationManagementService.addOrUpdateStorage(storage);

        return Response.ok().build();
    }

    @PUT
    @Path("/xml/storage")
    @Consumes (MediaType.APPLICATION_XML)
    public Response addOrUpdateStorageAsXML(Storage storage)
            throws IOException, JAXBException
    {
        configurationManagementService.addOrUpdateStorage(storage);

        return Response.ok().build();
    }

    @GET
    @Path("/storage/{storageId}")
    @Produces(MediaType.APPLICATION_XML)
    public Storage getStorage(@PathParam("storageId") final String storageId)
            throws IOException, ParseException
    {
        return configurationManagementService.getStorage(storageId);
    }

    @DELETE
    @Path("storage")
    public void removeStorage(@QueryParam("storageId") final String storageId)
            throws IOException, JAXBException
    {
        configurationManagementService.removeStorage(storageId);
    }

    @PUT
    @Path("repository/{storageId}/{repositoryId}")
    public Response addOrUpdateRepository(@PathParam("storageId") String storageId,
                                          @PathParam("repositoryId") String repositoryId,
                                          @QueryParam("policy") @DefaultValue("release") String policy,
                                          @QueryParam("implementation") @DefaultValue("file-system") String implementation,
                                          @QueryParam("type") @DefaultValue("hosted") String type,
                                          @QueryParam("secured") @DefaultValue("false") boolean secured,
                                          @QueryParam("trashEnabled") @DefaultValue("false") boolean trashEnabled,
                                          @QueryParam("allowsForceDeletion") @DefaultValue("false") boolean allowsForceDeletion,
                                          @QueryParam("allowsRedeployment") @DefaultValue("false") boolean allowsRedeployment)
            throws IOException, JAXBException
    {
        Repository repository = new Repository();
        repository.setId(repositoryId);
        repository.setPolicy(policy);
        repository.setImplementation(implementation);
        repository.setType(type);
        repository.setSecured(secured);
        repository.setTrashEnabled(trashEnabled);
        repository.setAllowsForceDeletion(allowsForceDeletion);
        repository.setAllowsRedeployment(allowsRedeployment);

        configurationManagementService.addOrUpdateRepository(storageId, repository);

        return Response.ok().build();
    }

    // TODO: Add a separate method for defining the repository's proxy settings.

    @GET
    @Path("repository/{storageId}/{repositoryId}")
    @Produces(MediaType.APPLICATION_XML)
    public Repository getRepository(@PathParam("storageId") final String storageId,
                                    @PathParam("repositoryId") final String repositoryId)
            throws IOException, ParseException
    {
        /*
        final GenericParser<Repository> parser = new GenericParser<Repository>()
        {
            @Override
            public XStream getXStreamInstance()
            {
                XStream xstream = new XStream();
                xstream.autodetectAnnotations(true);
                xstream.processAnnotations(Repository.class);

                return xstream;
            }
        };

        return Response.ok(new StreamingOutput()
        {
            @Override
            public void write(final OutputStream os)
                    throws IOException, WebApplicationException
            {
                parser.store(configurationManagementService.getStorage(storageId)
                                                           .getRepository(repositoryId), os);
            }
        }).type(MediaType.APPLICATION_XML).build();
        */

        @SuppressWarnings("UnnecessaryLocalVariable")
        Repository repository = configurationManagementService.getStorage(storageId) .getRepository(repositoryId);

        return repository;
    }

    @PUT
    @Path("/repository")
    @Consumes (MediaType.APPLICATION_XML)
    public void addOrUpdateRepository(String storageId)
            throws IOException, JAXBException
    {
        Repository repository = null;

        // ...

        configurationManagementService.addOrUpdateRepository(storageId, repository);
    }

    @DELETE
    @Path("repository")
    public void removeRepository(@QueryParam("storageId") final String storageId,
                                 @QueryParam("repositoryId") final String repositoryId)
            throws IOException
    {
        configurationManagementService.getStorage(storageId).removeRepository(repositoryId);
    }

}
