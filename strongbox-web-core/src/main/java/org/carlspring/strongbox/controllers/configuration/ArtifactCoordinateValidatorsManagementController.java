package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.storage.validation.ArtifactCoordinatesValidator;
import org.carlspring.strongbox.storage.validation.artifact.ArtifactCoordinatesValidatorRegistry;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 * @author Aditya Srinivasan
 */
@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping(value = "/api/configuration/artifact-coordinate-validators")
@Api(value = "/api/configuration/artifact-coordinate-validators")
public class ArtifactCoordinateValidatorsManagementController
        extends BaseController
{

    static final String SUCCESSFUL_LIST = "All version validators of the requested repository";
    
    static final String NOT_FOUND_STORAGE_MESSAGE = "Could not find requested storage ${storageId}.";
    
    static final String NOT_FOUND_REPOSITORY_MESSAGE = "Could not find requested repository ${storageId}:${repositoryId}.";
    
    static final String NOT_FOUND_LAYOUT_PROVIDER_MESSAGE = "Could not find requested artifact coordinate validator for layout provider ${layoutProvider}.";

    static final String SUCCESSFUL_ADD = "Version validator type was added to the requested repository.";

    static final String SUCCESSFUL_DELETE = "Version validator type was deleted from the requested repository.";
    
    static final String NOT_FOUND_ALIAS_MESSAGE = "Could not delete requested alias from the requested repository.";

    @Inject
    private ConfigurationManagementService configurationManagementService;
    
    @Inject
    private ArtifactCoordinatesValidatorRegistry artifactCoordinatesValidatorRegistry;
    
    
    @ApiOperation(value = "Enumerates all version validators of the requested repository")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_LIST),
                            @ApiResponse(code = 404, message = NOT_FOUND_REPOSITORY_MESSAGE) })
    @GetMapping(value = "/{storageId}/{repositoryId}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity listArtifactCoordinatesForRepository(@PathVariable String storageId,
                                                               @PathVariable String repositoryId,
                                                               @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        if (storage == null)
        {
            return getNotFoundResponseEntity(NOT_FOUND_STORAGE_MESSAGE, acceptHeader);
        }

        Repository repository = storage.getRepository(repositoryId);
        if (repository == null)
        {
            return getNotFoundResponseEntity(NOT_FOUND_REPOSITORY_MESSAGE, acceptHeader);
        }

        Set<String> versionValidators = repository.getArtifactCoordinateValidators()
                                                  .keySet()
                                                  .stream()
                                                  .map(String::toString)
                                                  .collect(Collectors.toSet());

        return getJSONListResponseEntityBody("versionValidators", versionValidators);
    }

    @ApiOperation(value = "Adds version validator type to the requested repository")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_ADD),
                            @ApiResponse(code = 404, message = NOT_FOUND_REPOSITORY_MESSAGE) })
    @PutMapping(value = "/{storageId}/{repositoryId}/{alias}",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity add(@PathVariable String storageId,
                              @PathVariable String repositoryId,
                              @PathVariable String alias,
                              @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        Configuration configuration = configurationManager.getConfiguration();
        Storage storage = configuration.getStorage(storageId);
        if (storage == null)
        {
            return getNotFoundResponseEntity(NOT_FOUND_STORAGE_MESSAGE, acceptHeader);
        }

        Repository repository = storage.getRepository(repositoryId);
        if (repository == null)
        {
            return getNotFoundResponseEntity(NOT_FOUND_REPOSITORY_MESSAGE, acceptHeader);
        }

        repository.getArtifactCoordinateValidators().put(alias, alias);
        configurationManagementService.save(configuration);

        return getSuccessfulResponseEntity(SUCCESSFUL_ADD, acceptHeader);
    }

    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_DELETE),
                            @ApiResponse(code = 404, message = NOT_FOUND_REPOSITORY_MESSAGE) })
    @DeleteMapping(value = "/{storageId}/{repositoryId}/{alias}",
                   produces = { MediaType.TEXT_PLAIN_VALUE,
                                MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity delete(@PathVariable String storageId,
                                 @PathVariable String repositoryId,
                                 @PathVariable String alias,
                                 @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        Configuration configuration = configurationManager.getConfiguration();
        Storage storage = configuration.getStorage(storageId);
        if (storage == null)
        {
            return getNotFoundResponseEntity(NOT_FOUND_STORAGE_MESSAGE, acceptHeader);
        }

        Repository repository = storage.getRepository(repositoryId);
        if (repository == null)
        {
            return getNotFoundResponseEntity(NOT_FOUND_REPOSITORY_MESSAGE, acceptHeader);
        }

        boolean resultOk = repository.getArtifactCoordinateValidators().remove(alias, alias);
        if (!resultOk)
        {
            return getNotFoundResponseEntity(NOT_FOUND_ALIAS_MESSAGE, acceptHeader);
        }

        configurationManagementService.save(configuration);
        return getSuccessfulResponseEntity(SUCCESSFUL_DELETE, acceptHeader);
    }

    @ApiOperation(value = "Returns a list of all the available artifact coordinate validators in the registry")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_LIST),
                            @ApiResponse(code = 404, message = NOT_FOUND_REPOSITORY_MESSAGE) })
    @GetMapping(value = "/validators",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity listArtifactCoordinateValidators(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        return getJSONListResponseEntityBody("versionValidators",
                                             artifactCoordinatesValidatorRegistry.getArtifactCoordinatesValidators()
                                                                                 .entrySet());
    }

    @ApiOperation(value = "Returns a list of artifact coordinate validators supported for a given layout provider")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_LIST),
                            @ApiResponse(code = 404, message = NOT_FOUND_REPOSITORY_MESSAGE) })
    @GetMapping(value = "/validators/{layoutProvider}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity listArtifactCoordinateValidatorsForLayoutProvider(@PathVariable String layoutProvider,
                                                                            @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        Map<String, Set<ArtifactCoordinatesValidator>> validators = artifactCoordinatesValidatorRegistry.getArtifactCoordinatesValidators();

        if (!validators.containsKey(layoutProvider))
        {
            return getNotFoundResponseEntity(NOT_FOUND_LAYOUT_PROVIDER_MESSAGE, acceptHeader);
        }
        
        return getJSONListResponseEntityBody("versionValidators", validators.get(layoutProvider));
    }
    
}
