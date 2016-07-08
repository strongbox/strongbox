package org.carlspring.strongbox.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.pool.PoolStats;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.HttpConnectionPool;
import org.carlspring.strongbox.storage.repository.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * @author korest
 */
@Component
@Path("/configuration/proxy/connection-pool")
@Api(value = "/configuration/proxy/connection-pool")
public class ProxyRepositoryConnectionPoolConfigurationManagementRestlet extends BaseRestlet
{

    @Autowired
    private ConfigurationManagementService configurationManagementService;

    @Autowired
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;

    @PUT
    @Path("{storageId}/{repositoryId}/{numberOfConnections}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Update number of pool connections pool for proxy repository")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Number of pool connections for proxy repository was updated successfully."),
            @ApiResponse(code = 500, message = "An error occurred.") })
    public Response setNumberOfConnectionsForProxyRepository(@PathParam(value = "storageId") String storageId,
            @PathParam(value = "repositoryId") String repositoryId, @PathParam(value = "numberOfConnections") int numberOfConnections)
            throws IOException, JAXBException
    {
        Storage storage = getConfiguration().getStorage(storageId);
        if(storage == null)
        {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("The storage does not exist!")
                    .build();
        }
        Repository repository = storage.getRepository(repositoryId);
        if(storage.getRepository(repositoryId) == null)
        {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("The repository does not exist!")
                    .build();
        }
        if(storage.getRepository(repositoryId).getRemoteRepository() == null)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Repository doesn't have remote repository!")
                    .build();
        }

        configurationManagementService.setProxyRepositoryMaxConnections(storageId, repositoryId, numberOfConnections);
        proxyRepositoryConnectionPoolConfigurationService.setMaxPerRepository(
                repository.getRemoteRepository().getUrl(), numberOfConnections);

        return Response.ok().entity("Number of pool connections for repository was updated successfully.").build();
    }

    @GET
    @Path("{storageId}/{repositoryId}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get proxy repository pool stats")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Proxy repository pool stats where retrieved."),
            @ApiResponse(code = 500, message = "An error occurred.") })
    public Response getPoolStatsForProxyRepository(@PathParam(value = "storageId") String storageId,
            @PathParam(value = "repositoryId") String repositoryId)
    {
        Storage storage = getConfiguration().getStorage(storageId);
        if(storage == null)
        {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("The storage does not exist!")
                    .build();
        }
        Repository repository = storage.getRepository(repositoryId);
        if(repository == null)
        {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("The repository does not exist!")
                    .build();
        }
        if(storage.getRepository(repositoryId).getRemoteRepository() == null)
        {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Repository doesn't have remote repository!")
                    .build();
        }

        PoolStats poolStats = proxyRepositoryConnectionPoolConfigurationService
                .getPoolStats(repository.getRemoteRepository().getUrl());

        return Response.ok().entity(poolStats.toString()).build();
    }

    @PUT
    @Path("default/{numberOfConnections}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Update default number of connections for proxy repository")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Default number of connections for proxy repository was updated successfully."),
            @ApiResponse(code = 500, message = "An error occurred.") })
    public Response setDefaultNumberOfConnectionsForProxyRepository(@PathParam(value = "numberOfConnections") int numberOfConnections)
    {
        proxyRepositoryConnectionPoolConfigurationService.setDefaultMaxPerRepository(numberOfConnections);
        return Response.ok().entity("Default number of connections for proxy repository was updated successfully.").build();
    }

    @GET
    @Path("default-number")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get default number of connections for proxy repository")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Default number of connections was retrieved."),
            @ApiResponse(code = 500, message = "An error occurred.") })
    public Response getDefaultNumberOfConnectionsForProxyRepository()
    {
        int defaultNumber = proxyRepositoryConnectionPoolConfigurationService.getDefaultMaxPerRepository();
        return Response.ok().entity(defaultNumber).build();
    }

    @PUT
    @Path("max/{numberOfConnections}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Update max number of connections for proxy repository")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Max number of connections for proxy repository was updated successfully."),
            @ApiResponse(code = 500, message = "An error occurred.") })
    public Response setMaxNumberOfConnectionsForProxyRepository(@PathParam(value = "numberOfConnections") int numberOfConnections)
    {
        proxyRepositoryConnectionPoolConfigurationService.setMaxTotal(numberOfConnections);
        return Response.ok().entity("Max number of connections for proxy repository was updated successfully.").build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value = "Get max number of connections for proxy repository")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Max number of connections for proxy repository was retrieved."),
            @ApiResponse(code = 500, message = "An error occurred.") })
    public Response getMaxNumberOfConnectionsForProxyRepository()
    {
        int maxNumberOfConnections = proxyRepositoryConnectionPoolConfigurationService.getTotalStats().getMax();
        return Response.ok().entity(maxNumberOfConnections).build();
    }
}
