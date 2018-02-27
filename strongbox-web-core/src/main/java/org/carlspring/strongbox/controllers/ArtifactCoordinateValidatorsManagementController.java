package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;

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
import org.springframework.web.bind.annotation.*;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping(value = "/configuration/artifact-coordinate-validators")
@Api(value = "/configuration/artifact-coordinate-validators")
public class ArtifactCoordinateValidatorsManagementController
        extends BaseController
{

    static final String SUCCESSFUL_LIST = "All version validators of the requested repository";
    static final String NOT_FOUND_STORAGE_MESSAGE = "Could not find requested storage ${storageId}.";
    static final String NOT_FOUND_REPOSITORY_MESSAGE = "Could not find requested repository ${storageId}:${repositoryId}.";

    static final String SUCCESSFUL_ADD = "Version validator type was added to the requested repository.";

    static final String SUCCESSFUL_DELETE = "Version validator type was deleted from the requested repository.";
    static final String NOT_FOUND_ALIAS_MESSAGE = "Could not delete requested alias from the requested repository.";

    @ApiOperation(value = "Enumerates all version validators of the requested repository ")
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

        repository.getArtifactCoordinateValidators().add(alias);

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

        boolean resultOk = repository.getArtifactCoordinateValidators().remove(alias);
        if (!resultOk)
        {
            return getNotFoundResponseEntity(NOT_FOUND_ALIAS_MESSAGE, acceptHeader);
        }

        return getSuccessfulResponseEntity(SUCCESSFUL_DELETE, acceptHeader);
    }
}
