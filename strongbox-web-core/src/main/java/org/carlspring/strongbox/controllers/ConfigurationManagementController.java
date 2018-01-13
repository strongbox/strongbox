package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.controllers.support.BaseUrlEntityBody;
import org.carlspring.strongbox.controllers.support.PortEntityBody;
import org.carlspring.strongbox.controllers.support.ResponseStatusEnum;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.routing.RoutingRule;
import org.carlspring.strongbox.storage.routing.RoutingRules;
import org.carlspring.strongbox.storage.routing.RuleSet;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

import io.swagger.annotations.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
@Controller
@RequestMapping("/configuration/strongbox")
@Api(value = "/configuration/strongbox")
public class ConfigurationManagementController
        extends BaseController
{

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationManagementController.class);

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @Inject
    private StorageManagementService storageManagementService;

    @Inject
    private RepositoryManagementService repositoryManagementService;

    @Inject
    private RepositoryIndexManager repositoryIndexManager;


    @ApiOperation(value = "Upload a strongbox.xml and reload the server's configuration.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The configuration was updated successfully."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_UPLOAD')")
    @RequestMapping(value = "/xml",
                    method = RequestMethod.PUT,
                    produces = MediaType.TEXT_PLAIN_VALUE,
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity setConfigurationXML(@ApiParam(value = "The strongbox.xml configuration file", required = true)
                                              @RequestBody Configuration configuration)
    {
        try
        {
            configurationManagementService.setConfiguration(configuration);

            logger.info("Received new configuration over REST.");

            return ResponseEntity.ok(ResponseStatusEnum.OK.value());
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(ResponseStatusEnum.FAILED.value());
        }
    }

    @ApiOperation(value = "Retrieves the strongbox.xml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = ""),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW')")
    @RequestMapping(value = "/xml",
                    method = RequestMethod.GET,
                    produces = { MediaType.APPLICATION_XML_VALUE,
                                 MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getConfigurationXML()
    {
        logger.debug("Received configuration request.");

        return ResponseEntity.status(HttpStatus.OK)
                             .body(getConfiguration());
    }

    @ApiOperation(value = "Updates the base URL of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The base URL was updated."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_BASE_URL')")
    @RequestMapping(value = "/baseUrl",
                    method = RequestMethod.PUT,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity setBaseUrl(@ApiParam(value = "The base URL", required = true)
                                     @RequestBody BaseUrlEntityBody baseUrlEntity)
    {
        try
        {
            String newBaseUrl = baseUrlEntity.getBaseUrl();
            configurationManagementService.setBaseUrl(newBaseUrl);

            logger.info("Set baseUrl to [{}].", newBaseUrl);

            return ResponseEntity.ok(ResponseStatusEnum.OK.value());
        }
        catch (IOException | JAXBException e)
        {
            logger.error("Error while updating the base URL of the service", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(ResponseStatusEnum.FAILED.value());
        }
    }

    @ApiOperation(value = "Returns the base URL of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "",
                                         response = String.class),
                            @ApiResponse(code = 404,
                                         message = "No value for baseUrl has been defined yet.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_BASE_URL')")
    @RequestMapping(value = "/baseUrl",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getBaseUrl()
            throws IOException
    {
        if (configurationManagementService.getBaseUrl() != null)
        {
            return ResponseEntity.ok(getBaseUrlEntityBody(configurationManagementService.getBaseUrl()));
        }
        else
        {
            String message = "No value for baseUrl has been defined yet.";
            return toResponseEntity(message, HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Sets the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The port was updated."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_PORT')")
    @RequestMapping(value = "/port/{port}",
                    method = RequestMethod.PUT,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity setPort(@ApiParam(value = "The port of the service", required = true)
                                  @PathVariable int port)
            throws IOException, JAXBException
    {
        try
        {
            configurationManagementService.setPort(port);

            logger.info("Set port to " + port + ". This operation will require a server restart.");

            return ResponseEntity.ok(ResponseStatusEnum.OK.value());
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(ResponseStatusEnum.FAILED.value());
        }
    }

    @ApiOperation(value = "Sets the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The port was updated."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_PORT')")
    @PutMapping(value = "/port",
                produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity setPort(@ApiParam(value = "The port of the service", required = true)
                                  @RequestBody PortEntityBody portEntity)
            throws IOException, JAXBException
    {
        return setPort(portEntity.getPort());
    }

    @ApiOperation(value = "Returns the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "",
                                         response = Integer.class) })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_PORT')")
    @RequestMapping(value = "/port",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getPort()
            throws IOException
    {
        return ResponseEntity.ok(getPortEntityBody(configurationManagementService.getPort()));
    }

    @ApiOperation(value = "Updates the proxy configuration for a repository, if one is specified, or, otherwise, the global proxy settings.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The proxy configuration was updated successfully."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_GLOBAL_PROXY_CFG')")
    @RequestMapping(value = "/proxy-configuration",
                    method = RequestMethod.PUT,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity setProxyConfiguration(@ApiParam(value = "The storageId")
                                                @RequestParam(value = "storageId", required = false) String storageId,
                                                @ApiParam(value = "The repositoryId")
                                                @RequestParam(value = "repositoryId", required = false) String repositoryId,
                                                @ApiParam(value = "The proxy configuration for this proxy repository", required = true)
                                                @RequestBody ProxyConfiguration proxyConfiguration)
    {
        logger.debug("Received proxy configuration \n" + proxyConfiguration);

        try
        {
            configurationManagementService.setProxyConfiguration(storageId, repositoryId, proxyConfiguration);
            return ResponseEntity.ok(ResponseStatusEnum.OK.value());
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(ResponseStatusEnum.FAILED.value());
        }
    }

    @ApiOperation(value = "Returns the proxy configuration for a repository, if one is specified, or, otherwise, the global proxy settings.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = ""),
                            @ApiResponse(code = 404,
                                         message = "The proxy configuration for '${storageId}:${repositoryId}' was not found.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_GLOBAL_PROXY_CFG')")
    @RequestMapping(value = "/proxy-configuration",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getProxyConfiguration(@ApiParam(value = "The storageId")
                                                @RequestParam(value = "storageId", required = false) String storageId,
                                                @ApiParam(value = "The repositoryId")
                                                @RequestParam(value = "repositoryId", required = false) String repositoryId)
            throws IOException,
                   JAXBException
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
            return ResponseEntity.status(HttpStatus.OK)
                                 .body(proxyConfiguration);
        }
        else
        {
            String message = "The proxy configuration" +
                             (storageId != null ? " for " + storageId + ":" + repositoryId : "") +
                             " was not found.";

            return toResponseEntity(message, HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Add/update a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The storage was updated successfully."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_ADD_UPDATE_STORAGE')")
    @RequestMapping(value = "/storages",
                    method = RequestMethod.PUT,
                    consumes = { MediaType.APPLICATION_XML_VALUE,
                                 MediaType.APPLICATION_JSON_VALUE },
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity saveStorage(@ApiParam(value = "The storage object", required = true)
                                      @RequestBody Storage storage)
            throws IOException, JAXBException
    {
        try
        {
            configurationManagementService.saveStorage(storage);

            if (!storage.existsOnFileSystem())
            {
                storageManagementService.createStorage(storage);
            }

            return ResponseEntity.ok(ResponseStatusEnum.OK.value());
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(ResponseStatusEnum.FAILED.value());
        }
    }

    @ApiOperation(value = "Retrieve the configuration of a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = ""),
                            @ApiResponse(code = 404,
                                         message = "Storage ${storageId} was not found.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_STORAGE_CONFIGURATION')")
    @RequestMapping(value = "/storages/{storageId}",
                    method = RequestMethod.GET,
                    consumes = { MediaType.TEXT_PLAIN_VALUE },
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getStorage(@ApiParam(value = "The storageId", required = true)
                                     @PathVariable final String storageId)
            throws IOException
    {
        final Storage storage = configurationManagementService.getStorage(storageId);

        if (storage != null)
        {
            return ResponseEntity.status(HttpStatus.OK)
                                 .body(storage);
        }
        else
        {
            String message = "Storage " + storageId + " was not found.";
            return toResponseEntity(message, HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Deletes a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The storage was removed successfully."),
                            @ApiResponse(code = 404,
                                         message = "Storage ${storageId} not found!"),
                            @ApiResponse(code = 500,
                                         message = "Failed to remove storage ${storageId}!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_DELETE_STORAGE_CONFIGURATION')")
    @RequestMapping(value = "/storages/{storageId}",
                    method = RequestMethod.DELETE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity removeStorage(@ApiParam(value = "The storageId", required = true)
                                        @PathVariable final String storageId,
                                        @ApiParam(value = "Whether to force delete and remove the storage from the file system")
                                        @RequestParam(name = "force", defaultValue = "false") final boolean force)
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

                return ResponseEntity.ok(ResponseStatusEnum.OK.value());
            }
            catch (IOException | JAXBException e)
            {
                logger.error(e.getMessage(), e);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .body(ResponseStatusEnum.FAILED.value());
            }
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Storage " + storageId + " not found.");
        }
    }

    @ApiOperation(value = "Adds or updates a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The repository was updated successfully."),
                            @ApiResponse(code = 404, message = "Repository ${repositoryId} not found!"),
                            @ApiResponse(code = 500, message = "Failed to remove repository ${repositoryId}!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_ADD_UPDATE_REPOSITORY')")
    @RequestMapping(value = "/storages/{storageId}/{repositoryId}",
                    method = RequestMethod.PUT,
                    consumes = { MediaType.APPLICATION_XML_VALUE,
                                 MediaType.APPLICATION_JSON_VALUE },
                    produces = { MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity addOrUpdateRepository(@ApiParam(value = "The storageId", required = true)
                                                @PathVariable String storageId,
                                                @ApiParam(value = "The repositoryId", required = true)
                                                @PathVariable String repositoryId,
                                                @ApiParam(value = "The repository object", required = true)
                                                @RequestBody Repository repository)
            throws IOException, JAXBException
    {
        try
        {
            logger.debug("Creating repository " + storageId + ":" + repositoryId + "...");

            repository.setStorage(configurationManagementService.getStorage(storageId));
            configurationManagementService.saveRepository(storageId, repository);

            final File repositoryBaseDir = new File(repository.getBasedir());
            if (!repositoryBaseDir.exists())
            {
                repositoryManagementService.createRepository(storageId, repository.getId());
            }

            return ResponseEntity.ok(ResponseStatusEnum.OK.value());
        }
        catch (IOException | JAXBException | RepositoryManagementStrategyException e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(ResponseStatusEnum.FAILED.value());
        }
    }

    @ApiOperation(value = "Returns the configuration of a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The repository was updated successfully.",
                                         response = Repository.class),
                            @ApiResponse(code = 404,
                                         message = "Repository ${storageId}:${repositoryId} was not found!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_REPOSITORY')")
    @RequestMapping(value = "/storages/{storageId}/{repositoryId}",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_XML_VALUE)
    @SuppressWarnings("UnnecessaryLocalVariable")
    public ResponseEntity getRepository(@ApiParam(value = "The storageId", required = true)
                                        @PathVariable final String storageId,
                                        @ApiParam(value = "The repositoryId", required = true)
                                        @PathVariable final String repositoryId)
            throws IOException, ParseException, JAXBException
    {

        try
        {
            Repository repository = configurationManagementService.getStorage(storageId)
                                                                  .getRepository(repositoryId);

            if (repository != null)
            {
                return ResponseEntity.status(HttpStatus.OK)
                                     .body(repository);
            }
            else
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                     .body("Repository " + storageId + ":" + repositoryId + " was not found.");
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Repository " + storageId + ":" + repositoryId + " was not found.");
        }
    }

    @ApiOperation(value = "Deletes a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The repository was deleted successfully."),
                            @ApiResponse(code = 404,
                                         message = "Repository ${storageId}:${repositoryId} was not found!"),
                            @ApiResponse(code = 500,
                                         message = "Failed to remove repository ${repositoryId}!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_DELETE_REPOSITORY')")
    @RequestMapping(value = "/storages/{storageId}/{repositoryId}",
                    method = RequestMethod.DELETE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity removeRepository(@ApiParam(value = "The storageId", required = true)
                                           @PathVariable final String storageId,
                                           @ApiParam(value = "The repositoryId", required = true)
                                           @PathVariable final String repositoryId,
                                           @ApiParam(value = "Whether to force delete the repository from the file system")
                                           @RequestParam(name = "force", defaultValue = "false") final boolean force)
            throws IOException
    {
        final Repository repository = configurationManagementService.getStorage(storageId)
                                                                    .getRepository(repositoryId);
        if (repository != null)
        {
            try
            {
                logger.debug(storageId);
                logger.debug(repositoryId);

                repositoryIndexManager.closeIndexer(storageId + ":" + repositoryId);

                final File repositoryBaseDir = new File(repository.getBasedir());
                if (!repositoryBaseDir.exists() && force)
                {
                    repositoryManagementService.removeRepository(storageId, repository.getId());
                }

                Configuration configuration = configurationManagementService.getConfiguration();
                Storage storage = configuration.getStorage(storageId);
                storage.removeRepository(repositoryId);

                configurationManagementService.saveStorage(storage);

                logger.debug("Removed repository " + storageId + ":" + repositoryId + ".");

                return ResponseEntity.ok(ResponseStatusEnum.OK.value());
            }
            catch (IOException | JAXBException e)
            {
                logger.error(e.getMessage(), e);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .body(ResponseStatusEnum.FAILED.value());
            }
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Repository " + storageId + ":" + repositoryId + " was not found.");
        }
    }

    @ApiOperation(value = "Returns the routing rules.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "", response = RoutingRules.class) })
    @RequestMapping(value = "/routing/rules",
                    method = RequestMethod.GET,
                    produces = { MediaType.APPLICATION_JSON_VALUE,
                                 MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity getRoutingRules()
    {
        return ResponseEntity.ok(configurationManagementService.getRoutingRules());
    }

    @ApiOperation(value = "Adds the accepted rules set.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
                            @ApiResponse(code = 404, message = "Element was not found.") })
    @RequestMapping(value = "/routing/rules/set/accepted",
                    method = RequestMethod.PUT,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity addAcceptedRuleSet(@RequestBody RuleSet ruleSet)
    {
        return getResponse(configurationManagementService.saveAcceptedRuleSet(ruleSet));
    }

    @ApiOperation(value = "Removes the accepted rules set.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
                            @ApiResponse(code = 404, message = "Element was not found.") })
    @RequestMapping(value = "/routing/rules/set/accepted/{groupRepository}",
                    method = RequestMethod.DELETE,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity removeAcceptedRuleSet(@PathVariable String groupRepository)
    {
        return getResponse(configurationManagementService.removeAcceptedRuleSet(groupRepository));
    }

    @ApiOperation(value = "Adds the accepted repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
                            @ApiResponse(code = 400, message = "Routing rule is empty"),
                            @ApiResponse(code = 404, message = "Element was not found.") })
    @RequestMapping(value = "/routing/rules/accepted/{groupRepository}/repositories",
                    method = RequestMethod.PUT,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity addAcceptedRepository(@PathVariable String groupRepository,
                                                @RequestBody RoutingRule routingRule)
    {
        logger.debug("[addAcceptedRepository] Routing rule " + routingRule);

        if (routingRule.getPattern() == null && routingRule.getRepositories()
                                                           .isEmpty())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Routing rule is empty");
        }

        return getResponse(configurationManagementService.saveAcceptedRepository(groupRepository, routingRule));
    }

    @ApiOperation(value = "Removes the accepted repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
                            @ApiResponse(code = 404, message = "Element was not found.") })
    @RequestMapping(value = "/routing/rules/accepted/{groupRepository}/repositories/{repositoryId}",
                    method = RequestMethod.DELETE,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity removeAcceptedRepository(@PathVariable String groupRepository,
                                                   @PathVariable String repositoryId,
                                                   @RequestParam("pattern") String pattern)
    {
        return getResponse(
                configurationManagementService.removeAcceptedRepository(groupRepository, pattern, repositoryId));
    }

    @ApiOperation(value = "Overrides the accepted repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "OK"),
                            @ApiResponse(code = 400, message = "Routing rule is empty"),
                            @ApiResponse(code = 404, message = "Element was not found.") })
    @RequestMapping(value = "/routing/rules/accepted/{groupRepository}/override/repositories",
                    method = RequestMethod.PUT,
                    consumes = MediaType.APPLICATION_JSON_VALUE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity overrideAcceptedRepository(@PathVariable String groupRepository,
                                                     @RequestBody RoutingRule routingRule)
    {
        logger.debug("[addAcceptedRepository] Routing rule " + routingRule);

        if (routingRule.getPattern() == null && routingRule.getRepositories().isEmpty())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Routing rule is empty");
        }

        return getResponse(configurationManagementService.overrideAcceptedRepositories(groupRepository, routingRule));
    }

    private ResponseEntity getResponse(boolean result)
    {
        if (result)
        {
            return ResponseEntity.ok(ResponseStatusEnum.OK.value());
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Element was not found.");
        }
    }

}
