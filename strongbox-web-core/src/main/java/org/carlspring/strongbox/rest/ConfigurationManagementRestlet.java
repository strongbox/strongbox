package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.configuration.ProxyConfiguration;
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

    @PUT
    @Path("/proxy-configuration")
    @Produces(MediaType.APPLICATION_XML)
    public Response setProxyConfiguration(@QueryParam("storageId") String storageId,
                                          @QueryParam("repositoryId") String repositoryId,
                                          ProxyConfiguration proxyConfiguration)
            throws IOException, JAXBException
    {
        configurationManagementService.setProxyConfiguration(storageId, repositoryId, proxyConfiguration);

        return Response.ok().build();
    }

    @GET
    @Path("/proxy-configuration")
    @Produces(MediaType.APPLICATION_XML)
    public ProxyConfiguration getProxyConfiguration(@QueryParam("storageId") String storageId,
                                                    @QueryParam("repositoryId") String repositoryId)
            throws IOException, JAXBException
    {
        if (storageId == null)
        {
            return configurationManagementService.getProxyConfiguration();
        }
        else
        {
            return configurationManagementService.getStorage(storageId)
                                                 .getRepository(repositoryId)
                                                 .getProxyConfiguration();
        }
    }

    @PUT
    @Path("/storages")
    @Consumes(MediaType.APPLICATION_XML)
    public Response addOrUpdateStorage(Storage storage)
            throws IOException, JAXBException
    {
        configurationManagementService.addOrUpdateStorage(storage);

        return Response.ok().build();
    }

    @GET
    @Path("/storages/{storageId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getStorage(@PathParam("storageId") final String storageId)
            throws IOException, ParseException
    {
        final Storage storage = configurationManagementService.getStorage(storageId);

        if (storage != null)
        {
            return Response.status(Response.Status.OK).entity(storage).build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/storages/{storageId}")
    public Response removeStorage(@PathParam("storageId") final String storageId)
            throws IOException, JAXBException
    {
        if (configurationManagementService.getStorage(storageId) != null)
        {
            configurationManagementService.removeStorage(storageId);

            logger.debug("Removed storage " + storageId + ".");

            return Response.ok().build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/storages/{storageId}")
    @Consumes(MediaType.APPLICATION_XML)
    public Response addOrUpdateRepository(@PathParam("storageId") final String storageId,
                                          Repository repository)
            throws IOException, JAXBException
    {
        configurationManagementService.addOrUpdateRepository(storageId, repository);

        return Response.ok().build();
    }

    @GET
    @Path("/storages/{storageId}/{repositoryId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getRepository(@PathParam("storageId") final String storageId,
                                  @PathParam("repositoryId") final String repositoryId)
            throws IOException, ParseException
    {
        @SuppressWarnings("UnnecessaryLocalVariable")
        Repository repository = configurationManagementService.getStorage(storageId).getRepository(repositoryId);

        if (repository != null)
        {
            return Response.status(Response.Status.OK).entity(repository).build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/storages/{storageId}/{repositoryId}")
    @Consumes (MediaType.APPLICATION_XML)
    public Response addOrUpdateRepository(@PathParam("storageId") final String storageId,
                                          @PathParam("repositoryId") final String repositoryId,
                                          Repository repository)
            throws IOException, JAXBException
    {
        configurationManagementService.addOrUpdateRepository(storageId, repository);

        return Response.ok().build();
    }

    @DELETE
    @Path("/storages/{storageId}/{repositoryId}")
    public Response removeRepository(@PathParam("storageId") final String storageId,
                                     @PathParam("repositoryId") final String repositoryId)
            throws IOException
    {
        if (configurationManagementService.getStorage(storageId).getRepository(repositoryId) != null)
        {
            configurationManagementService.getStorage(storageId).removeRepository(repositoryId);

            logger.debug("Removed repository " + storageId + ":" + repositoryId + ".");

            return Response.ok().build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}
