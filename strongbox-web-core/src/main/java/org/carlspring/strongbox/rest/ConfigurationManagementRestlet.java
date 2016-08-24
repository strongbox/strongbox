package org.carlspring.strongbox.rest;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.security.exceptions.AuthenticationException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

import io.swagger.annotations.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * @author mtodorov
 */
@Component
@Path("/configuration/strongbox")
@Api(value = "/configuration/strongbox")
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
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Upload a strongbox.xml and reload the server's configuration.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The configuration was updated successfully."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_UPLOAD')")
    public Response setConfigurationXML(@ApiParam(value = "The strongbox.xml configuration file", required = true)
                                        Configuration configuration)
            throws IOException,
                   AuthenticationException,
                   JAXBException
    {
        try
        {
            configurationManagementService.setConfiguration(configuration);

            logger.info("Received new configuration over REST.");

            return Response.ok()
                           .entity("The configuration was updated successfully.")
                           .build();
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/xml")
    @Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieves the strongbox.xml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW')")
    public Response getConfigurationXML()
            throws IOException, ParseException
    {
        logger.debug("Received configuration request.");

        return Response.status(Response.Status.OK).entity(getConfiguration()).build();
    }

    @PUT
    @Path("/baseUrl/{baseUrl:.*}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Retrieves the strongbox.xml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The base URL was updated."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_BASE_URL')")
    public Response setBaseUrl(@ApiParam(value = "The base URL", required = true)
                               @PathParam("baseUrl") String baseUrl)
            throws IOException,
                   AuthenticationException,
                   JAXBException
    {
        try
        {
            configurationManagementService.setBaseUrl(baseUrl);

            logger.info("Set baseUrl to " + baseUrl + ".");

            return Response.ok()
                           .entity("The base URL was updated.")
                           .build();
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
    @ApiOperation(value = "Sets the base URL of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "", response = String.class),
                            @ApiResponse(code = 404, message = "No value for baseUrl has been defined yet.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_BASE_URL')")
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
    @ApiOperation(value = "Sets the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The port was updated."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_PORT')")
    public Response setPort(@ApiParam(value = "The port of the service", required = true)
                            @PathParam("port") int port)
            throws IOException, JAXBException
    {
        try
        {
            configurationManagementService.setPort(port);

            logger.info("Set port to " + port + ". This operation will require a server restart.");

            return Response.ok()
                           .entity("The port was updated.")
                           .build();
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
    @ApiOperation(value = "Returns the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "", response = Integer.class) })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_PORT')")
    public int getPort()
            throws IOException,
                   AuthenticationException
    {
        return configurationManagementService.getPort();
    }

    @PUT
    @Path("/proxy-configuration")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Updates the proxy configuration for a repository, if one is specified, or, otherwise, the global proxy settings.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The proxy configuration was updated successfully."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_GLOBAL_PROXY_CFG')")
    public Response setProxyConfiguration(@ApiParam(value = "The storageId", required = true)
                                          @QueryParam("storageId") String storageId,
                                          @ApiParam(value = "The repositoryId", required = true)
                                          @QueryParam("repositoryId") String repositoryId,
                                          @ApiParam(value = "The proxy configuration for this proxy repository", required = true)
                                          ProxyConfiguration proxyConfiguration)
            throws IOException, JAXBException
    {
        try
        {
            configurationManagementService.setProxyConfiguration(storageId, repositoryId, proxyConfiguration);

            return Response.ok().entity("The proxy configuration was updated successfully.").build();
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/proxy-configuration")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Returns the proxy configuration for a repository, if one is specified, or, otherwise, the global proxy settings.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 404, message = "The proxy configuration for '${storageId}:${repositoryId}' was not found.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_GLOBAL_PROXY_CFG')")
    public Response getProxyConfiguration(@ApiParam(value = "The storageId", required = true)
                                          @QueryParam("storageId") String storageId,
                                          @ApiParam(value = "The repositoryId", required = true)
                                          @QueryParam("repositoryId") String repositoryId)
            throws IOException, JAXBException
    {
        ProxyConfiguration proxyConfiguration;
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
            String message = "The proxy configuration" +
                             (storageId != null ? " for " + storageId + ":" + repositoryId : "") +
                             " was not found.";

            return Response.status(Response.Status.NOT_FOUND)
                           .entity(message)
                           .build();
        }
    }

    @PUT
    @Path("/storages")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Add/update a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The storage was updated successfully."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_ADD_UPDATE_STORAGE')")
    public Response addOrUpdateStorage(@ApiParam(value = "The storage object", required = true)
                                       Storage storage)
            throws IOException, JAXBException
    {
        try
        {
            configurationManagementService.addOrUpdateStorage(storage);

            if (!storage.existsOnFileSystem())
            {
                storageManagementService.createStorage(storage);
            }

            return Response.ok()
                           .entity("The storage was updated successfully.")
                           .build();
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/storages/{storageId}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Retrieve the configuration of a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 404, message = "Storage ${storageId} was not found.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_STORAGE_CONFIGURATION')")
    public Response getStorage(@ApiParam(value = "The storageId", required = true)
                               @PathParam("storageId") final String storageId)
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
                           .entity("Storage " + storageId + " was not found.")
                           .build();
        }
    }

    @DELETE
    @Path("/storages/{storageId}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Deletes a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The storage was removed successfully."),
                            @ApiResponse(code = 404, message = "Storage ${storageId} not found!"),
                            @ApiResponse(code = 500, message = "Failed to remove storage ${storageId}!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_DELETE_STORAGE_CONFIGURATION')")
    public Response removeStorage(@ApiParam(value = "The storageId", required = true)
                                  @PathParam("storageId") final String storageId,
                                  @ApiParam(value = "Whether to force delete and remove the storage from the file system", required = true)
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

                return Response.ok()
                               .entity("The storage was removed successfully.")
                               .build();
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
    @Path("/storages/{storageId}/{repositoryId}")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Adds or updates a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The repository was updated successfully."),
                            @ApiResponse(code = 404, message = "Repository ${repositoryId} not found!"),
                            @ApiResponse(code = 500, message = "Failed to remove repository ${repositoryId}!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_ADD_UPDATE_REPOSITORY')")
    public Response addOrUpdateRepository(@ApiParam(value = "The repositoryId", required = true)
                                          @PathParam("storageId") String storageId,
                                          @ApiParam(value = "The repositoryId", required = true)
                                          @PathParam("repositoryId") String repositoryId,
                                          @ApiParam(value = "The repository object", required = true) Repository repository)
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

            return Response.ok()
                           .entity("The repository was updated successfully.")
                           .build();
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GET
    @Path("/storages/{storageId}/{repositoryId}")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @ApiOperation(value = "Returns the configuration of a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The repository was updated successfully.", response = Repository.class),
                            @ApiResponse(code = 404, message = "Repository ${storageId}:${repositoryId} was not found!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_REPOSITORY')")
    public Response getRepository(@ApiParam(value = "The storageId", required = true)
                                  @PathParam("storageId") final String storageId,
                                  @ApiParam(value = "The repositoryId", required = true)
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
                           .entity("Repository " + storageId + ":" + repositoryId + " was not found.")
                           .build();
        }
    }

    @DELETE
    @Path("/storages/{storageId}/{repositoryId}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Deletes a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The repository was deleted successfully."),
                            @ApiResponse(code = 404, message = "Repository ${storageId}:${repositoryId} was not found!"),
                            @ApiResponse(code = 500, message = "Failed to remove repository ${repositoryId}!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_DELETE_REPOSITORY')")
    public Response removeRepository(@ApiParam(value = "The storageId", required = true)
                                     @PathParam("storageId") final String storageId,
                                     @ApiParam(value = "The repositoryId", required = true)
                                     @PathParam("repositoryId") final String repositoryId,
                                     @ApiParam(value = "Whether to force delete the repository from the file system")
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

                Configuration configuration = configurationManagementService.getConfiguration();
                Storage storage = configuration.getStorage(storageId);
                storage.removeRepository(repositoryId);

                configurationManagementService.addOrUpdateStorage(storage);

                logger.debug("Removed repository " + storageId + ":" + repositoryId + ".");

                return Response.ok().build();
            }
            catch (IOException | JAXBException e)
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
                           .entity("Repository " + storageId + ":" + repositoryId + " was not found.")
                           .build();
        }
    }

    @GET
    @Path("/routing/rules")
    @Produces({ MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_XML })
    public Response getRoutingRules()
    {
        return Response.ok(configurationManagementService.getRoutingRules()).build();
    }

    @PUT
    @Path("/routing/rules/set/accepted")
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response addAcceptedRuleSet(RuleSet ruleSet)
    {
        final boolean added = configurationManagementService.addOrUpdateAcceptedRuleSet(ruleSet);
        if (added)
        {
            return Response.ok().build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/routing/rules/set/accepted/{groupRepository}")
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response removeAcceptedRuleSet(@PathParam("groupRepository") String groupRepository)
    {
        return getResponse(configurationManagementService.removeAcceptedRuleSet(groupRepository));
    }

    @PUT
    @Path("/routing/rules/accepted/{groupRepository}/repositories")
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response addAcceptedRepository(@PathParam("groupRepository") String groupRepository,
                                          RoutingRule routingRule)
    {
        return getResponse(configurationManagementService.addOrUpdateAcceptedRepository(groupRepository, routingRule));
    }

    @DELETE
    @Path("/routing/rules/accepted/{groupRepository}/repositories/{repositoryId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response removeAcceptedRepository(@PathParam("groupRepository") String groupRepository,
                                             @PathParam("repositoryId") String repositoryId,
                                             @QueryParam("pattern") String pattern)
    {
        return getResponse(configurationManagementService.removeAcceptedRepository(groupRepository, pattern, repositoryId));
    }

    @PUT
    @Path("/routing/rules/accepted/{groupRepository}/override/repositories")
    @Consumes({ MediaType.APPLICATION_JSON })
    public Response overrideAcceptedRepository(@PathParam("groupRepository") String groupRepository,
                                               RoutingRule routingRule)
    {
        return getResponse(configurationManagementService.overrideAcceptedRepositories(groupRepository, routingRule));
    }

    private Response getResponse(boolean result)
    {
        if (result)
        {
            return Response.ok().build();
        }
        else
        {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}
