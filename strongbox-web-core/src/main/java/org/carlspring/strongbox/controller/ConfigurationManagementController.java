package org.carlspring.strongbox.controller;

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
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.lucene.queryparser.classic.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author mtodorov
 */
@Controller
@RequestMapping("/configuration/strongbox")
public class ConfigurationManagementController
        extends BaseController
{

    private static final Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    private ConfigurationManagementService configurationManagementService;

    @Autowired
    private StorageManagementService storageManagementService;

    @Autowired
    private RepositoryManagementService repositoryManagementService;

    @Autowired
    private RepositoryIndexManager repositoryIndexManager;


    @ApiOperation(value = "Upload a strongbox.xml and reload the server's configuration.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The configuration was updated successfully."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_UPLOAD')")
    @RequestMapping(value = "/xml", method = RequestMethod.PUT, produces = MediaType.TEXT_PLAIN_VALUE,
            consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity setConfigurationXML(@RequestBody String serializedConfiguration)
            throws IOException,
                   AuthenticationException,
                   JAXBException
    {
        GenericParser<Configuration> parser = new GenericParser<>(Configuration.class);
        Configuration configuration = parser.deserialize(serializedConfiguration);

        try
        {
            configurationManagementService.setConfiguration(configuration);

            logger.info("Received new configuration over REST.");

            return ResponseEntity.ok("The configuration was updated successfully.");
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retrieves the strongbox.xml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW')")
    @RequestMapping(value = "/xml", method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML_VALUE,
                                                                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getConfigurationXML()
            throws IOException, ParseException, JAXBException
    {
        logger.debug("Received configuration request.");

        GenericParser<Configuration> parser = new GenericParser<>(Configuration.class);
        String serializedConfiguration = parser.serialize(getConfiguration());

        return ResponseEntity.status(HttpStatus.OK).body(serializedConfiguration);
    }

    @ApiOperation(value = "Retrieves the strongbox.xml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The base URL was updated."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_BASE_URL')")
    @RequestMapping(value = "/baseUrl", method = RequestMethod.PUT, consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity setBaseUrl(@RequestBody String newBaseUrl)
            throws IOException,
                   AuthenticationException,
                   JAXBException
    {
        try
        {
            configurationManagementService.setBaseUrl(newBaseUrl);

            logger.info("Set baseUrl to [" + newBaseUrl + "].");

            return ResponseEntity.ok("The base URL was updated.");
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Sets the base URL of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "", response = String.class),
                            @ApiResponse(code = 404, message = "No value for baseUrl has been defined yet.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_BASE_URL')")
    @RequestMapping(value = "/baseUrl", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getBaseUrl()
            throws IOException,
                   AuthenticationException
    {
        if (configurationManagementService.getBaseUrl() != null)
        {
            return ResponseEntity.status(HttpStatus.OK).body(
                    "{\"baseUrl\":\"" + configurationManagementService.getBaseUrl() + "\"}");
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No value for baseUrl has been defined yet.");
        }
    }

    @ApiOperation(value = "Sets the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The port was updated."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_PORT')")
    @RequestMapping(value = "port/{port}", method = RequestMethod.PUT, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity setPort(@PathVariable int port)
            throws IOException, JAXBException
    {
        try
        {
            configurationManagementService.setPort(port);

            logger.info("Set port to " + port + ". This operation will require a server restart.");

            return ResponseEntity.ok("The port was updated.");
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Returns the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "", response = Integer.class) })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_PORT')")
    @RequestMapping(value = "/port", method = RequestMethod.GET)
    public ResponseEntity getPort()
            throws IOException,
                   AuthenticationException
    {
        return ResponseEntity.ok(configurationManagementService.getPort());
    }

    @ApiOperation(value = "Updates the proxy configuration for a repository, if one is specified, or, otherwise, the global proxy settings.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The proxy configuration was updated successfully."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_GLOBAL_PROXY_CFG')")
    @RequestMapping(value = "/proxy-configuration", method = RequestMethod.PUT, consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity setProxyConfiguration(@RequestParam(value = "storageId", required = false) String storageId,
                                                @RequestParam(value = "repositoryId", required = false) String repositoryId,
                                                @RequestBody String serializedProxyConfiguration)
            throws IOException,
                   JAXBException
    {
        GenericParser<ProxyConfiguration> parser = new GenericParser<>(ProxyConfiguration.class);
        ProxyConfiguration proxyConfiguration = parser.deserialize(serializedProxyConfiguration);

        logger.debug("Received proxy configuration \n" + proxyConfiguration);

        try
        {
            configurationManagementService.setProxyConfiguration(storageId, repositoryId, proxyConfiguration);
            return ResponseEntity.ok("The proxy configuration was updated successfully.");
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Returns the proxy configuration for a repository, if one is specified, or, otherwise, the global proxy settings.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 404, message = "The proxy configuration for '${storageId}:${repositoryId}' was not found.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_GLOBAL_PROXY_CFG')")
    @RequestMapping(value = "/proxy-configuration", method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity getProxyConfiguration(@RequestParam(value = "storageId", required = false) String storageId,
                                                @RequestParam(value = "repositoryId", required = false) String repositoryId)
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
            GenericParser<ProxyConfiguration> parser = new GenericParser<>(ProxyConfiguration.class);
            String serializedProxyConfiguration = parser.serialize(proxyConfiguration);

            return ResponseEntity.status(HttpStatus.OK).body(serializedProxyConfiguration);
        }
        else
        {
            String message = "The proxy configuration" +
                             (storageId != null ? " for " + storageId + ":" + repositoryId : "") +
                             " was not found.";

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
    }

    @ApiOperation(value = "Add/update a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The storage was updated successfully."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_ADD_UPDATE_STORAGE')")
    @RequestMapping(value = "/storages",
                    method = RequestMethod.PUT,
                    consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addOrUpdateStorage(@RequestBody Storage storage)
            throws IOException, JAXBException
    {
        try
        {
            configurationManagementService.addOrUpdateStorage(storage);

            if (!storage.existsOnFileSystem())
            {
                storageManagementService.createStorage(storage);
            }

            return ResponseEntity.ok("The storage was updated successfully.");
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retrieve the configuration of a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 404, message = "Storage ${storageId} was not found.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_STORAGE_CONFIGURATION')")
    @RequestMapping(value = "/storages/{storageId}", method = RequestMethod.GET,
            consumes = { MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity getStorage(@PathVariable final String storageId)
            throws IOException, ParseException
    {
        final Storage storage = configurationManagementService.getStorage(storageId);

        if (storage != null)
        {
            GenericParser<Storage> parser = new GenericParser<>(Storage.class);
            String serializedStorage = null;
            try
            {
                serializedStorage = parser.serialize(storage);
            }
            catch (JAXBException e)
            {
                e.printStackTrace();
            }

            return ResponseEntity.status(HttpStatus.OK).body(serializedStorage);
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Storage " + storageId + " was not found.");
        }
    }

    @ApiOperation(value = "Deletes a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The storage was removed successfully."),
                            @ApiResponse(code = 404, message = "Storage ${storageId} not found!"),
                            @ApiResponse(code = 500, message = "Failed to remove storage ${storageId}!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_DELETE_STORAGE_CONFIGURATION')")
    @RequestMapping(value = "/storages/{storageId}", method = RequestMethod.DELETE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity removeStorage(@PathVariable final String storageId,
                                        @RequestParam(name = "force", defaultValue = "false", required = true) final boolean force)
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

                return ResponseEntity.ok("The storage was removed successfully.");
            }
            catch (IOException | JAXBException e)
            {
                logger.error(e.getMessage(), e);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        "Failed to remove storage " + storageId + "!");
            }
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Storage " + storageId + " not found.");
        }
    }

    @ApiOperation(value = "Adds or updates a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The repository was updated successfully."),
                            @ApiResponse(code = 404, message = "Repository ${repositoryId} not found!"),
                            @ApiResponse(code = 500, message = "Failed to remove repository ${repositoryId}!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_ADD_UPDATE_REPOSITORY')")
    @RequestMapping(value = "/storages/{storageId}/{repositoryId}",
                    method = RequestMethod.PUT,
                    consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addOrUpdateRepository(@PathVariable String storageId,
                                                @PathVariable String repositoryId,
                                                @RequestBody Repository repository)
            throws IOException, JAXBException
    {
        try
        {
            logger.debug("Creating repository " + storageId + ":" + repositoryId + "...");

            repository.setStorage(configurationManagementService.getStorage(storageId));
            configurationManagementService.addOrUpdateRepository(storageId, repository);

            final File repositoryBaseDir = new File(repository.getBasedir());
            if (!repositoryBaseDir.exists())
            {
                repositoryManagementService.createRepository(storageId, repository.getId());
            }

            return ResponseEntity.ok("The repository was updated successfully.");
        }
        catch (IOException | JAXBException e)
        {
            logger.error(e.getMessage(), e);

            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Returns the configuration of a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The repository was updated successfully.",
            response = Repository.class),
                            @ApiResponse(code = 404,
                                    message = "Repository ${storageId}:${repositoryId} was not found!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_REPOSITORY')")
    @RequestMapping(value = "/storages/{storageId}/{repositoryId}", method = RequestMethod.GET,
            produces = MediaType.TEXT_PLAIN_VALUE)
    @SuppressWarnings("UnnecessaryLocalVariable")
    public ResponseEntity getRepository(@PathVariable final String storageId,
                                        @PathVariable final String repositoryId)
            throws IOException, ParseException, JAXBException
    {

        try
        {
            Repository repository = configurationManagementService.getStorage(storageId).getRepository(repositoryId);

            if (repository != null)
            {
                GenericParser<Repository> parser = new GenericParser<>(Repository.class);
                String serializeRepository = parser.serialize(repository);

                return ResponseEntity.status(HttpStatus.OK).body(serializeRepository);
            }
            else
            {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        "Repository " + storageId + ":" + repositoryId + " was not found.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    "Repository " + storageId + ":" + repositoryId + " was not found.");
        }
    }

    @ApiOperation(value = "Deletes a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The repository was deleted successfully."),
                            @ApiResponse(code = 404,
                                    message = "Repository ${storageId}:${repositoryId} was not found!"),
                            @ApiResponse(code = 500, message = "Failed to remove repository ${repositoryId}!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_DELETE_REPOSITORY')")
    @RequestMapping(value = "/storages/{storageId}/{repositoryId}", method = RequestMethod.DELETE,
            produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity removeRepository(@PathVariable final String storageId,
                                           @PathVariable final String repositoryId,
                                           @RequestParam(name = "force", defaultValue = "false") boolean force)
            throws IOException
    {
        final Repository repository = configurationManagementService.getStorage(storageId).getRepository(repositoryId);
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

                configurationManagementService.addOrUpdateStorage(storage);

                logger.debug("Removed repository " + storageId + ":" + repositoryId + ".");

                return ResponseEntity.ok().build();
            }
            catch (IOException | JAXBException e)
            {
                logger.error(e.getMessage(), e);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        "Failed to remove repository " + repositoryId + "!");
            }
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    "Repository " + storageId + ":" + repositoryId + " was not found.");
        }
    }

    @RequestMapping(value = "/routing/rules", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE,
                                                                                       MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity getRoutingRules()
    {
        return ResponseEntity.ok(configurationManagementService.getRoutingRules());
    }

    @RequestMapping(value = "/routing/rules/set/accepted", method = RequestMethod.PUT, consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity addAcceptedRuleSet(@RequestBody String serializeRuleSet)
    {

        GenericParser<RuleSet> parser = new GenericParser<>(RuleSet.class);
        RuleSet ruleSet = null;
        try
        {
            ruleSet = parser.deserialize(serializeRuleSet);
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

        final boolean added = configurationManagementService.addOrUpdateAcceptedRuleSet(ruleSet);
        if (added)
        {
            return ResponseEntity.ok().build();
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @RequestMapping(value = "/routing/rules/set/accepted/{groupRepository}", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity removeAcceptedRuleSet(@PathVariable String groupRepository)
    {
        return getResponse(configurationManagementService.removeAcceptedRuleSet(groupRepository));
    }

    @RequestMapping(value = "/routing/rules/accepted/{groupRepository}/repositories", method = RequestMethod.PUT, consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity addAcceptedRepository(@PathVariable String groupRepository,
                                                @RequestBody String serializeRoutingRule)
    {

        GenericParser<RoutingRule> parser = new GenericParser<>(RoutingRule.class);
        RoutingRule routingRule = null;
        try
        {
            routingRule = parser.deserialize(serializeRoutingRule);
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

        logger.debug("[addAcceptedRepository] Routing rule " + routingRule);

        if (routingRule.getPattern() == null && routingRule.getRepositories().isEmpty())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Routing rule is empty");
        }

        return getResponse(configurationManagementService.addOrUpdateAcceptedRepository(groupRepository, routingRule));
    }

    @RequestMapping(value = "/routing/rules/accepted/{groupRepository}/repositories/{repositoryId}", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity removeAcceptedRepository(@PathVariable String groupRepository,
                                                   @PathVariable String repositoryId,
                                                   @RequestParam("pattern") String pattern)
    {
        return getResponse(
                configurationManagementService.removeAcceptedRepository(groupRepository, pattern, repositoryId));
    }

    @RequestMapping(value = "/routing/rules/accepted/{groupRepository}/override/repositories", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity overrideAcceptedRepository(@PathVariable String groupRepository,
                                                     @RequestBody String serializeRoutingRule)
    {

        GenericParser<RoutingRule> parser = new GenericParser<>(RoutingRule.class);
        RoutingRule routingRule = null;
        try
        {
            routingRule = parser.deserialize(serializeRoutingRule);
        }
        catch (JAXBException e)
        {
            e.printStackTrace();
        }

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
            return ResponseEntity.ok().build();
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
