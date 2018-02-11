package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.authentication.registry.support.AuthenticatorsScanner;

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
@RequestMapping(value = "/configuration/authenticators")
@Api(value = "/configuration/authenticators")
public class AuthenticatorsConfigController
        extends BaseController
{

    static final String SUCCESSFUL_REORDER = "Reorder succeeded.";
    static final String FAILED_REORDER = "Could not reorder authenticators registry.";

    static final String SUCCESSFUL_RELOAD = "Reload succeeded.";
    static final String FAILED_RELOAD = "Could not reload authenticators registry.";

    private final AuthenticatorsRegistry authenticatorsRegistry;

    private final AuthenticatorsScanner authenticatorsScanner;

    public AuthenticatorsConfigController(AuthenticatorsRegistry authenticatorsRegistry,
                                          AuthenticatorsScanner authenticatorsScanner)
    {
        this.authenticatorsRegistry = authenticatorsRegistry;
        this.authenticatorsScanner = authenticatorsScanner;
    }

    @ApiOperation(value = "Enumerates ordered collection of authenticators with order number and name")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The list was returned successfully.") })
    @GetMapping(value = "/",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity list()
    {
        return ResponseEntity.ok(authenticatorsRegistry);
    }

    @ApiOperation(value = "Reorders authenticators by their indexes")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_REORDER),
                            @ApiResponse(code = 400, message = FAILED_REORDER) })
    @PutMapping(value = "/reorder/{first}/{second}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity reorder(@PathVariable int first,
                                  @PathVariable int second,
                                  @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        try
        {
            authenticatorsRegistry.reorder(first, second);
            return getSuccessfulResponseEntity(SUCCESSFUL_REORDER, acceptHeader);
        }
        catch (Exception e)
        {
            return getBadRequestResponseEntity(FAILED_REORDER, acceptHeader);
        }
    }

    @ApiOperation(value = "Reloads authenticators registry")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_RELOAD),
                            @ApiResponse(code = 400, message = FAILED_RELOAD) })
    @PutMapping(value = "/reload",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = { MediaType.TEXT_PLAIN_VALUE,
                         MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity reload(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        try
        {
            authenticatorsScanner.scanAndReloadRegistry();
            return getSuccessfulResponseEntity(SUCCESSFUL_RELOAD, acceptHeader);
        }
        catch (Exception e)
        {
            return getBadRequestResponseEntity(FAILED_RELOAD, acceptHeader);
        }
    }
}
