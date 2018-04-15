package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.services.support.ConfigurationException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.repository.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Pablo Tirado
 */
@Controller
@RequestMapping("/api/configuration/strongbox/storages")
@Api(value = "/api/configuration/strongbox/storages")
public class StoragesConfigurationController
        extends BaseConfigurationController
{

    private final StorageManagementService storageManagementService;

    private final RepositoryManagementService repositoryManagementService;

    private final Optional<RepositoryIndexManager> repositoryIndexManager;

    public StoragesConfigurationController(ConfigurationManagementService configurationManagementService,
                                           StorageManagementService storageManagementService,
                                           RepositoryManagementService repositoryManagementService,
                                           Optional<RepositoryIndexManager> repositoryIndexManager)
    {
        super(configurationManagementService);
        this.storageManagementService = storageManagementService;
        this.repositoryManagementService = repositoryManagementService;
        this.repositoryIndexManager = repositoryIndexManager;
    }

    @ApiOperation(value = "Add/update a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The storage was updated successfully."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_ADD_UPDATE_STORAGE')")
    @RequestMapping(value = "",
                    method = RequestMethod.PUT,
                    consumes = { MediaType.APPLICATION_XML_VALUE,
                                 MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity saveStorage(@ApiParam(value = "The storage object", required = true)
                                      @RequestBody Storage storage)
    {
        try
        {
            configurationManagementService.saveStorage(storage);

            if (!storage.existsOnFileSystem())
            {
                storageManagementService.createStorage(storage);
            }

            return ResponseEntity.ok("The storage was updated successfully.");
        }
        catch (ConfigurationException | IOException e)
        {
            logger.error(e.getMessage(), e);

            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retrieve the configuration of a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = ""),
                            @ApiResponse(code = 404,
                                         message = "Storage ${storageId} was not found.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_STORAGE_CONFIGURATION')")
    @RequestMapping(value = "/{storageId}",
                    method = RequestMethod.GET,
                    consumes = { MediaType.TEXT_PLAIN_VALUE })
    public ResponseEntity getStorage(@ApiParam(value = "The storageId", required = true)
                                     @PathVariable final String storageId)
    {
        final Storage storage = configurationManagementService.getStorage(storageId);

        if (storage != null)
        {
            return ResponseEntity.status(HttpStatus.OK)
                                 .body(storage);
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Storage " + storageId + " was not found.");
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
    @RequestMapping(value = "/{storageId}",
                    method = RequestMethod.DELETE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity removeStorage(@ApiParam(value = "The storageId", required = true)
                                        @PathVariable final String storageId,
                                        @ApiParam(value = "Whether to force delete and remove the storage from the file system")
                                        @RequestParam(name = "force", defaultValue = "false") final boolean force)
    {
        if (configurationManagementService.getStorage(storageId) != null)
        {
            try
            {
                repositoryIndexManager.ifPresent(
                        repositoryIndexManager -> repositoryIndexManager.closeIndexersForStorage(storageId));

                if (force)
                {
                    storageManagementService.removeStorage(storageId);
                }

                configurationManagementService.removeStorage(storageId);

                logger.debug("Removed storage " + storageId + ".");

                return ResponseEntity.ok("The storage was removed successfully.");
            }
            catch (ConfigurationException | IOException e)
            {
                logger.error(e.getMessage(), e);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .body("Failed to remove storage " + storageId + "!");
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
    @RequestMapping(value = "/{storageId}/{repositoryId}",
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
    {
        try
        {
            logger.debug("Creating repository " + storageId + ":" + repositoryId + "...");

            repository.setStorage(configurationManagementService.getStorage(storageId));
            configurationManagementService.saveRepository(storageId, repository);

            final RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);
            if (!Files.exists(repositoryPath))
            {
                repositoryManagementService.createRepository(storageId, repository.getId());
            }

            return ResponseEntity.ok("The repository was updated successfully.");
        }
        catch (IOException | ConfigurationException | RepositoryManagementStrategyException e)
        {
            logger.error(e.getMessage(), e);

            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Returns the configuration of a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The repository was updated successfully.",
                                         response = Repository.class),
                            @ApiResponse(code = 404,
                                         message = "Repository ${storageId}:${repositoryId} was not found!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_REPOSITORY')")
    @RequestMapping(value = "/{storageId}/{repositoryId}",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity getRepository(@ApiParam(value = "The storageId", required = true)
                                        @PathVariable final String storageId,
                                        @ApiParam(value = "The repositoryId", required = true)
                                        @PathVariable final String repositoryId)
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
    @RequestMapping(value = "/{storageId}/{repositoryId}",
                    method = RequestMethod.DELETE,
                    produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity removeRepository(@ApiParam(value = "The storageId", required = true)
                                           @PathVariable final String storageId,
                                           @ApiParam(value = "The repositoryId", required = true)
                                           @PathVariable final String repositoryId,
                                           @ApiParam(value = "Whether to force delete the repository from the file system")
                                           @RequestParam(name = "force", defaultValue = "false") final boolean force)
    {
        final Repository repository = configurationManagementService.getStorage(storageId)
                                                                    .getRepository(repositoryId);
        if (repository != null)
        {
            try
            {
                if (repositoryIndexManager.isPresent())
                {
                    repositoryIndexManager.get().closeIndexer(storageId + ":" + repositoryId);
                }

                final RepositoryPath repositoryPath = repositoryPathResolver.resolve(repository);
                if (!Files.exists(repositoryPath) && force)
                {
                    repositoryManagementService.removeRepository(storageId, repository.getId());
                }

                Configuration configuration = getConfiguration();
                Storage storage = configuration.getStorage(storageId);
                storage.removeRepository(repositoryId);

                configurationManagementService.saveStorage(storage);

                logger.debug("Removed repository " + storageId + ":" + repositoryId + ".");

                return ResponseEntity.ok().build();
            }
            catch (IOException | ConfigurationException e)
            {
                logger.error(e.getMessage(), e);

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .body("Failed to remove repository " + repositoryId + "!");
            }
        }
        else
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body("Repository " + storageId + ":" + repositoryId + " was not found.");
        }
    }
}
