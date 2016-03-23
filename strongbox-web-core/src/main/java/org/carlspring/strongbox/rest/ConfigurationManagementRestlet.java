package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.security.jaas.authentication.AuthenticationException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.File;
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

    @Autowired
    private StorageManagementService storageManagementService;

    @Autowired
    private RepositoryManagementService repositoryManagementService;

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;


    @PUT
    @Path("/xml")
    public Response setConfiguration(Configuration configuration)
            throws IOException,
                   AuthenticationException,
                   JAXBException
    {
        try
        {
            configurationManagementService.setConfiguration(configuration);

            logger.info("Received new configuration over REST.");

            return Response.ok().build();
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/xml")
    @Produces(MediaType.APPLICATION_XML)
    public Response getConfiguration()
            throws IOException, ParseException
    {
        logger.debug("Received configuration request.");

        return Response.status(Response.Status.OK).entity(configurationManagementService.getConfiguration()).build();
    }

    @PUT
    @Path("/baseUrl/{baseUrl:.*}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response setBaseUrl(@PathParam("baseUrl") String baseUrl)
            throws IOException,
                   AuthenticationException,
                   JAXBException
    {
        try
        {
            configurationManagementService.setBaseUrl(baseUrl);

            logger.info("Set baseUrl to " + baseUrl);

            return Response.ok().build();
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/baseUrl")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getBaseUrl()
            throws IOException,
                   AuthenticationException
    {
        if (configurationManagementService.getBaseUrl() != null)
        {
            return Response.status(Response.Status.OK).entity(configurationManagementService.getBaseUrl()).build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).entity("No value for baseUrl has been defined yet.").build();
        }
    }

    @PUT
    @Path("/port/{port}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response setPort(@PathParam("port") int port)
            throws IOException, JAXBException
    {
        try
        {
            configurationManagementService.setPort(port);

            logger.info("Set port to " + port + ". This operation will require a server restart.");

            return Response.ok().build();
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
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
    @Consumes(MediaType.APPLICATION_XML)
    public Response setProxyConfiguration(@QueryParam("storageId") String storageId,
                                          @QueryParam("repositoryId") String repositoryId,
                                          ProxyConfiguration proxyConfiguration)
            throws IOException, JAXBException
    {
        try
        {
            configurationManagementService.setProxyConfiguration(storageId, repositoryId, proxyConfiguration);

            return Response.ok().build();
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/proxy-configuration")
    @Produces(MediaType.APPLICATION_XML)
    public Response getProxyConfiguration(@QueryParam("storageId") String storageId,
                                          @QueryParam("repositoryId") String repositoryId)
            throws IOException, JAXBException
    {
        ProxyConfiguration proxyConfiguration = null;
        if (storageId == null)
        {
            proxyConfiguration = configurationManagementService.getProxyConfiguration();
        }
        else
        {
            proxyConfiguration = configurationManagementService.getStorage(storageId)
                                                               .getRepository(repositoryId)
                                                               .getProxyConfiguration();
        }

        if (proxyConfiguration != null)
        {
            return Response.status(Response.Status.OK).entity(proxyConfiguration).build();
        }
        else
        {
            String message = "Proxy configuration" +
                             (storageId != null ? " for " + storageId + ":" + repositoryId : "") +
                             " not found.";

            return Response.status(Response.Status.NOT_FOUND)
                           .entity(message)
                           .build();
        }
    }

    @PUT
    @Path("/storages")
    @Consumes(MediaType.APPLICATION_XML)
    public Response addOrUpdateStorage(Storage storage)
            throws IOException, JAXBException
    {
        try
        {
            configurationManagementService.addOrUpdateStorage(storage);

            if (!storage.existsOnFileSystem())
            {
                storageManagementService.createStorage(storage);
            }

            return Response.ok().build();
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
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
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Storage " + storageId + " not found.")
                           .build();
        }
    }

    @DELETE
    @Path("/storages/{storageId}")
    public Response removeStorage(@PathParam("storageId") final String storageId,
                                  @QueryParam("force") @DefaultValue("false") final boolean force)
            throws IOException, JAXBException
    {
        if (configurationManagementService.getStorage(storageId) != null)
        {
            try
            {
                repositoryIndexManager.closeIndexersForStorage(storageId);

                if (force)
                {
                    storageManagementService.removeStorage(storageId);
                }

                configurationManagementService.removeStorage(storageId);

                logger.debug("Removed storage " + storageId + ".");

                return Response.ok().build();
            }
            catch (IOException | JAXBException e)
            {
                logger.error(e.getMessage(), e);

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity("Failed to remove storage " + storageId + "!")
                               .build();
            }
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Storage " + storageId + " not found.")
                           .build();
        }
    }

    @PUT
    @Path("/storages/{storageId}")
    @Consumes(MediaType.APPLICATION_XML)
    public Response addOrUpdateRepository(@PathParam("storageId") final String storageId,
                                          Repository repository)
            throws IOException, JAXBException
    {
        try
        {
            repository.setStorage(configurationManagementService.getStorage(storageId));
            configurationManagementService.addOrUpdateRepository(storageId, repository);

            final File repositoryBaseDir = new File(repository.getBasedir());
            if (!repositoryBaseDir.exists())
            {
                repositoryManagementService.createRepository(storageId, repository.getId());
            }

            return Response.ok().build();
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
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
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Repository " + storageId + ":" + repositoryId + " not found.")
                           .build();
        }
    }

    @DELETE
    @Path("/storages/{storageId}/{repositoryId}")
    public Response removeRepository(@PathParam("storageId") final String storageId,
                                     @PathParam("repositoryId") final String repositoryId,
                                     @QueryParam("force") @DefaultValue("false") boolean force)
            throws IOException
    {
        final Repository repository = configurationManagementService.getStorage(storageId).getRepository(repositoryId);
        if (repository != null)
        {
            try
            {
                repositoryIndexManager.closeIndexer(storageId + ":" + repositoryId);

                final File repositoryBaseDir = new File(repository.getBasedir());
                if (!repositoryBaseDir.exists() && force)
                {
                    repositoryManagementService.removeRepository(storageId, repository.getId());
                }

                configurationManagementService.getStorage(storageId).removeRepository(repositoryId);

                logger.debug("Removed repository " + storageId + ":" + repositoryId + ".");

                return Response.ok().build();
            }
            catch (IOException e)
            {
                logger.error(e.getMessage(), e);

                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                               .entity("Failed to remove repository " + repositoryId + "!")
                               .build();
            }
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Repository " + storageId + ":" + repositoryId + " not found.")
                           .build();
        }
    }

}
