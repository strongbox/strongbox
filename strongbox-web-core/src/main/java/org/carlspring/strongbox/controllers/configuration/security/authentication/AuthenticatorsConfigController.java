package org.carlspring.strongbox.controllers.configuration.security.authentication;

import org.carlspring.strongbox.authentication.registry.AuthenticationProvidersRegistry;
import org.carlspring.strongbox.authentication.registry.AuthenticationProvidersRegistry;
import org.carlspring.strongbox.authentication.registry.support.ConfigurableProviderManager;
import org.carlspring.strongbox.controllers.BaseController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/configuration/authenticators")
@Api(value = "/api/configuration/authenticators")
public class AuthenticatorsConfigController
        extends BaseController
{

    static final String SUCCESSFUL_REORDER = "Re-order succeeded.";

    static final String FAILED_REORDER = "Could not reorder authenticators registry.";

    static final String SUCCESSFUL_RELOAD = "Reload succeeded.";

    static final String FAILED_RELOAD = "Could not reload authenticators registry.";

    private final AuthenticationProvidersRegistry authenticationProvidersRegistry;

    private final ConfigurableProviderManager authenticatorsScanner;


    public AuthenticatorsConfigController(AuthenticationProvidersRegistry authenticatorsRegistry,
                                          ConfigurableProviderManager authenticatorsScanner)
    {
        this.authenticationProvidersRegistry = authenticatorsRegistry;
        this.authenticatorsScanner = authenticatorsScanner;
    }

    @ApiOperation(value = "Enumerates ordered collection of authenticators with order number and name")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The list was returned successfully.") })
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity list()
    {
        return ResponseEntity.ok(authenticationProvidersRegistry);
    }

    @ApiOperation(value = "Reorders authenticators by their indexes")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_REORDER),
                            @ApiResponse(code = 400, message = FAILED_REORDER) })
    @PutMapping(path = "/reorder/{first}/{second}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity reorder(@PathVariable int first,
                                  @PathVariable int second,
                                  @RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        try
        {
            authenticationProvidersRegistry.reorder(first, second);
            return getSuccessfulResponseEntity(SUCCESSFUL_REORDER, acceptHeader);
        }
        catch (Exception e)
        {
            return getExceptionResponseEntity(HttpStatus.BAD_REQUEST, FAILED_REORDER, e, acceptHeader);
        }
    }

    @ApiOperation(value = "Reloads authenticators registry")
    @ApiResponses(value = { @ApiResponse(code = 200, message = SUCCESSFUL_RELOAD),
                            @ApiResponse(code = 500, message = FAILED_RELOAD) })
    @PutMapping(path = "/reload",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity reload(@RequestHeader(HttpHeaders.ACCEPT) String acceptHeader)
    {
        try
        {
            authenticatorsScanner.reloadRegistry();
            return getSuccessfulResponseEntity(SUCCESSFUL_RELOAD, acceptHeader);
        }
        catch (Exception e)
        {
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, FAILED_RELOAD, e, acceptHeader);
        }
    }
}
