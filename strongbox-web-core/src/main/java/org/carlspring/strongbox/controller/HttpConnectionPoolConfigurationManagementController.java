package org.carlspring.strongbox.controller;

import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.IOException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.http.pool.PoolStats;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author korest
 */
@Controller
@RequestMapping("/configuration/proxy/connection-pool")
@Api(value = "/configuration/proxy/connection-pool")
@PreAuthorize("hasAuthority('ADMIN')")
public class HttpConnectionPoolConfigurationManagementController
        extends BaseController
{

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private ProxyRepositoryConnectionPoolConfigurationService proxyRepositoryConnectionPoolConfigurationService;


    @ApiOperation(value = "Update number of pool connections pool for proxy repository")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "Number of pool connections for proxy repository was updated successfully."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @RequestMapping(value = "{storageId}/{repositoryId}/{numberOfConnections}",
                    method = RequestMethod.PUT,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity setNumberOfConnectionsForProxyRepository(@PathVariable(value = "storageId") String storageId,
                                                                   @PathVariable(value = "repositoryId")
                                                                           String repositoryId,
                                                                   @PathVariable(value = "numberOfConnections")
                                                                           int numberOfConnections
    )
            throws IOException, JAXBException
    {

        Storage storage = getConfiguration().getStorage(storageId);
        if (storage == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The storage does not exist!");
        }

        Repository repository = storage.getRepository(repositoryId);
        if (storage.getRepository(repositoryId) == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The repository does not exist!");
        }

        if (storage.getRepository(repositoryId)
                   .getRemoteRepository() == null)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(
                                         "Repository doesn't have remote repository!");
        }

        configurationManagementService.setProxyRepositoryMaxConnections(storageId, repositoryId, numberOfConnections);
        proxyRepositoryConnectionPoolConfigurationService.setMaxPerRepository(
                repository.getRemoteRepository()
                          .getUrl(), numberOfConnections);

        return ResponseEntity.ok("Number of pool connections for repository was updated successfully.");
    }

    @ApiOperation(value = "Get proxy repository pool stats")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "Proxy repository pool stats where retrieved."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @RequestMapping(value = "{storageId}/{repositoryId}",
                    method = RequestMethod.GET,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity getPoolStatsForProxyRepository(@PathVariable(value = "storageId") String storageId,
                                                         @PathVariable(value = "repositoryId") String repositoryId)
    {
        Storage storage = getConfiguration().getStorage(storageId);
        if (storage == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The storage does not exist!");
        }

        Repository repository = storage.getRepository(repositoryId);
        if (repository == null)
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("The repository does not exist!");
        }

        if (storage.getRepository(repositoryId)
                   .getRemoteRepository() == null)
        {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(
                                         "Repository doesn't have remote repository!");
        }

        PoolStats poolStats = proxyRepositoryConnectionPoolConfigurationService
                                      .getPoolStats(repository.getRemoteRepository()
                                                              .getUrl());

        return ResponseEntity.ok(poolStats.toString());
    }

    @ApiOperation(value = "Update default number of connections for proxy repository")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "Default number of connections for proxy repository was updated successfully."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @RequestMapping(value = "default/{numberOfConnections}",
                    method = RequestMethod.PUT,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity setDefaultNumberOfConnectionsForProxyRepository(@PathVariable(value = "numberOfConnections")
                                                                                  int numberOfConnections)
    {
        proxyRepositoryConnectionPoolConfigurationService.setDefaultMaxPerRepository(numberOfConnections);
        return ResponseEntity.ok("Default number of connections for proxy repository was updated successfully.");
    }

    @ApiOperation(value = "Get default number of connections for proxy repository")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "Default number of connections was retrieved."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @RequestMapping(value = "default-number",
                    method = RequestMethod.GET,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity getDefaultNumberOfConnectionsForProxyRepository()
    {
        int defaultNumber = proxyRepositoryConnectionPoolConfigurationService.getDefaultMaxPerRepository();
        return ResponseEntity.ok(Integer.toString(defaultNumber));
    }

    @ApiOperation(value = "Update max number of connections for proxy repository")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "Max number of connections for proxy repository was updated successfully."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @RequestMapping(value = "max/{numberOfConnections}",
                    method = RequestMethod.PUT,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity setMaxNumberOfConnectionsForProxyRepository(@PathVariable(value = "numberOfConnections")
                                                                              int numberOfConnections)
    {
        proxyRepositoryConnectionPoolConfigurationService.setMaxTotal(numberOfConnections);
        return ResponseEntity.ok("Max number of connections for proxy repository was updated successfully.");
    }

    @ApiOperation(value = "Get max number of connections for proxy repository")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "Max number of connections for proxy repository was retrieved."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @RequestMapping(method = RequestMethod.GET,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity getMaxNumberOfConnectionsForProxyRepository()
    {
        int maxNumberOfConnections = proxyRepositoryConnectionPoolConfigurationService.getTotalStats()
                                                                                      .getMax();
        return ResponseEntity.ok(Integer.toString(maxNumberOfConnections));
    }

}
