package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.forms.configuration.ProxyConfigurationForm.ProxyConfigurationFormChecks;
import org.carlspring.strongbox.forms.configuration.RepositoryForm;
import org.carlspring.strongbox.forms.configuration.StorageForm;
import org.carlspring.strongbox.providers.io.RepositoryPath;
import org.carlspring.strongbox.repository.RepositoryManagementStrategyException;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.RepositoryManagementService;
import org.carlspring.strongbox.services.StorageManagementService;
import org.carlspring.strongbox.services.support.ConfigurationException;
import org.carlspring.strongbox.storage.MutableStorage;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.Views;
import org.carlspring.strongbox.storage.indexing.RepositoryIndexManager;
import org.carlspring.strongbox.storage.repository.MutableRepository;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.validation.RequestBodyValidationException;

import javax.validation.groups.Default;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.*;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Pablo Tirado
 */
@RestController
@RequestMapping("/api/configuration/strongbox/storages")
@Api(value = "/api/configuration/strongbox/storages")
public class StoragesConfigurationController
        extends BaseConfigurationController
{

    private static final String FAILED_SAVE_STORAGE = "Storage cannot be saved because the submitted form contains errors!";

    private static final String FAILED_SAVE_REPOSITORY = "Repository cannot be saved because the submitted form contains errors!";

    private static final String SUCCESSFUL_STORAGE_SAVE = "Storage was saved successfully.";

    private static final String FAILED_STORAGE_SAVE = "Storage was not saved.";

    private static final String SUCCESSFUL_REPOSITORY_SAVE = "repository was updated successfully.";

    private static final String FAILED_REPOSITORY_SAVE = "Repository was not saved.";

    private static final String SUCCESSFUL_STORAGE_REMOVAL = "Storage was removed successfully.";

    private static final String SUCCESSFUL_REPOSITORY_REMOVAL = "Repository was removed successfully.";

    private static final String FAILED_STORAGE_REMOVAL = "Failed to remove storage !";

    private static final String STORAGE_NOT_FOUND = "Storage was not found.";

    private static final String FAILED_REPOSITORY_REMOVAL = "Failed to remove repository !";

    private static final String REPOSITORY_NOT_FOUND = "Repository was not found.";

    private static final String FAILED_GET_REPOSITORY = "Failed to get repository !";

    private final StorageManagementService storageManagementService;

    private final RepositoryManagementService repositoryManagementService;

    private final Optional<RepositoryIndexManager> repositoryIndexManager;

    private final ConversionService conversionService;

    public StoragesConfigurationController(ConfigurationManagementService configurationManagementService,
                                           StorageManagementService storageManagementService,
                                           RepositoryManagementService repositoryManagementService,
                                           ConversionService conversionService,
                                           Optional<RepositoryIndexManager> repositoryIndexManager)
    {
        super(configurationManagementService);
        this.storageManagementService = storageManagementService;
        this.repositoryManagementService = repositoryManagementService;
        this.conversionService = conversionService;
        this.repositoryIndexManager = repositoryIndexManager;
    }

    @ApiOperation(value = "Add/update a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The storage was updated successfully."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_ADD_UPDATE_STORAGE')")
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = { MediaType.TEXT_PLAIN_VALUE,
                                                                          MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity saveStorage(
            @RequestBody @Validated({ Default.class,
                                      ProxyConfigurationFormChecks.class }) StorageForm storageForm,
            BindingResult bindingResult,
            @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException(FAILED_SAVE_STORAGE, bindingResult);
        }
        try
        {
            MutableStorage storage = conversionService.convert(storageForm, MutableStorage.class);
            configurationManagementService.saveStorage(storage);

            if (!storage.existsOnFileSystem())
            {
                storageManagementService.createStorage(storage);
            }

            return getSuccessfulResponseEntity(SUCCESSFUL_STORAGE_SAVE, accept);
        }
        catch (ConfigurationException | IOException e)
        {
            logger.error(e.getMessage(), e);

            return getFailedResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_STORAGE_SAVE, accept);
        }
    }

    @JsonView(Views.ShortStorage.class)
    @ApiOperation(value = "Retrieve the basic info about storages.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_STORAGE_CONFIGURATION')")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getStorages()
    {
        final List<Storage> storages = new ArrayList<>(configurationManagementService.getConfiguration()
                                                                                     .getStorages()
                                                                                     .values());
        return ResponseEntity.ok(new StoragesOutput(storages));
    }

    @JsonView(Views.LongStorage.class)
    @ApiOperation(value = "Retrieve the configuration of a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 404, message = "Storage ${storageId} was not found.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_STORAGE_CONFIGURATION')")
    @GetMapping(value = "/{storageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getStorage(@ApiParam(value = "The storageId", required = true)
                                     @PathVariable final String storageId)
    {
        final Storage storage = configurationManagementService.getConfiguration().getStorage(storageId);

        if (storage != null)
        {
            return ResponseEntity.ok(storage);
        }
        else
        {
            return getFailedResponseEntity(HttpStatus.NOT_FOUND, STORAGE_NOT_FOUND, MediaType.APPLICATION_JSON_VALUE);
        }
    }

    @ApiOperation(value = "Deletes a storage.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The storage was removed successfully."),
                            @ApiResponse(code = 404, message = "Storage ${storageId} not found!"),
                            @ApiResponse(code = 500, message = "Failed to remove storage ${storageId}!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_DELETE_STORAGE_CONFIGURATION')")
    @DeleteMapping(value = "/{storageId}", produces = { MediaType.TEXT_PLAIN_VALUE,
                                                        MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity removeStorage(@ApiParam(value = "The storageId", required = true)
                                        @PathVariable final String storageId,
                                        @ApiParam(value = "Whether to force delete and remove the storage from the file system")
                                        @RequestParam(name = "force", defaultValue = "false") final boolean force,
                                        @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        if (configurationManagementService.getConfiguration().getStorage(storageId) != null)
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

                return getSuccessfulResponseEntity(SUCCESSFUL_STORAGE_REMOVAL, accept);
            }
            catch (ConfigurationException | IOException e)
            {
                logger.error(e.getMessage(), e);

                return getFailedResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_STORAGE_REMOVAL, accept);
            }
        }
        else
        {
            return getFailedResponseEntity(HttpStatus.NOT_FOUND, STORAGE_NOT_FOUND, accept);
        }
    }

    @ApiOperation(value = "Adds or updates a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The repository was updated successfully."),
                            @ApiResponse(code = 404, message = "Repository ${repositoryId} not found!"),
                            @ApiResponse(code = 500, message = "Failed to remove repository ${repositoryId}!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_ADD_UPDATE_REPOSITORY')")
    @PutMapping(value = "/{storageId}/{repositoryId}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addOrUpdateRepository(@ApiParam(value = "The storageId", required = true)
                                                @PathVariable String storageId,
                                                @ApiParam(value = "The repositoryId", required = true)
                                                @PathVariable String repositoryId,
                                                @ApiParam(value = "The repository object", required = true)
                                                @RequestBody @Validated({ Default.class,
                                                                          ProxyConfigurationFormChecks.class })
                                                        RepositoryForm repositoryForm,
                                                BindingResult bindingResult,
                                                @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {

        if (configurationManagementService.getConfiguration().getStorage(storageId) != null)
        {
            if (bindingResult.hasErrors())
            {
                throw new RequestBodyValidationException(FAILED_SAVE_REPOSITORY, bindingResult);
            }

            try
            {
                MutableRepository repository = conversionService.convert(repositoryForm, MutableRepository.class);

                logger.debug("Creating repository " + storageId + ":" + repositoryId + "...");

                configurationManagementService.saveRepository(storageId, repository);

                final RepositoryPath repositoryPath = repositoryPathResolver.resolve(new Repository(repository));
                if (!Files.exists(repositoryPath))
                {
                    repositoryManagementService.createRepository(storageId, repository.getId());
                }

                return getSuccessfulResponseEntity(SUCCESSFUL_REPOSITORY_SAVE, accept);
            }
            catch (IOException | ConfigurationException | RepositoryManagementStrategyException e)
            {
                logger.error(e.getMessage(), e);

                return getFailedResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_REPOSITORY_SAVE, accept);
            }
        }
        else
        {
            return getFailedResponseEntity(HttpStatus.NOT_FOUND, STORAGE_NOT_FOUND, accept);
        }
    }

    @ApiOperation(value = "Returns the configuration of a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The repository was updated successfully.",
                                         response = MutableRepository.class),
                            @ApiResponse(code = 404,
                                         message = "Repository ${storageId}:${repositoryId} was not found!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_REPOSITORY')")
    @GetMapping(value = "/{storageId}/{repositoryId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getRepository(@ApiParam(value = "The storageId", required = true)
                                        @PathVariable final String storageId,
                                        @ApiParam(value = "The repositoryId", required = true)
                                        @PathVariable final String repositoryId)
    {
        try
        {
            Repository repository = configurationManagementService.getConfiguration()
                                                                  .getStorage(storageId)
                                                                  .getRepository(repositoryId);

            if (repository != null)
            {
                return ResponseEntity.ok(repository);
            }
            else
            {
                return getFailedResponseEntity(HttpStatus.NOT_FOUND, REPOSITORY_NOT_FOUND,
                                               MediaType.APPLICATION_JSON_VALUE);
            }
        }
        catch (Exception e)
        {
            logger.error(e.getMessage(), e);

            return getFailedResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_GET_REPOSITORY,
                                           MediaType.APPLICATION_JSON_VALUE);
        }
    }

    @ApiOperation(value = "Deletes a repository.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The repository was deleted successfully."),
                            @ApiResponse(code = 404, message = "Repository ${storageId}:${repositoryId} was not found!"),
                            @ApiResponse(code = 500, message = "Failed to remove repository ${repositoryId}!") })
    @PreAuthorize("hasAuthority('CONFIGURATION_DELETE_REPOSITORY')")
    @DeleteMapping(value = "/{storageId}/{repositoryId}", produces = { MediaType.TEXT_PLAIN_VALUE,
                                                                       MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity removeRepository(@ApiParam(value = "The storageId", required = true)
                                           @PathVariable final String storageId,
                                           @ApiParam(value = "The repositoryId", required = true)
                                           @PathVariable final String repositoryId,
                                           @ApiParam(value = "Whether to force delete the repository from the file system")
                                           @RequestParam(name = "force", defaultValue = "false") final boolean force,
                                           @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        final Repository repository = configurationManagementService.getConfiguration()
                                                                    .getStorage(storageId)
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

                configurationManagementService.removeRepository(storageId, repositoryId);

                logger.debug("Removed repository " + storageId + ":" + repositoryId + ".");

                return getSuccessfulResponseEntity(SUCCESSFUL_REPOSITORY_REMOVAL, accept);
            }
            catch (IOException | ConfigurationException e)
            {
                logger.error(e.getMessage(), e);

                return getFailedResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_REPOSITORY_REMOVAL, accept);
            }
        }
        else
        {
            return getFailedResponseEntity(HttpStatus.NOT_FOUND, REPOSITORY_NOT_FOUND, accept);
        }
    }

}
