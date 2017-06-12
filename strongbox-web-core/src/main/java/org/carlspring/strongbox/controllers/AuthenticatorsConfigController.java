package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.authentication.registry.AuthenticatorsRegistry;
import org.carlspring.strongbox.authentication.registry.support.AuthenticatorsScanner;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Przemyslaw Fusik
 */
@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping(value = "/configuration/authenticators")
@Api(value = "/configuration/authenticators")
public class AuthenticatorsConfigController
        extends BaseController
{

    @Inject
    private AuthenticatorsRegistry authenticatorsRegistry;

    @Inject
    private AuthenticatorsScanner authenticatorsScanner;

    @ApiOperation(value = "Enumerates ordered collection of authenticators with order number and name")
    @ApiResponses(value = { @ApiResponse(code = 200,
            message = "The list was returned successfully.") })
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML_VALUE })
    public ResponseEntity<String> list()
            throws JAXBException
    {
        final GenericParser<AuthenticatorsRegistry> parser = new GenericParser<>(AuthenticatorsRegistry.class);
        final String result = parser.serialize(authenticatorsRegistry);

        return ResponseEntity.ok(result);
    }

    @ApiOperation(value = "Reorders authenticators by their indexes")
    @ApiResponses(value = { @ApiResponse(code = 200,
            message = "Reorder succeeded"),
                            @ApiResponse(code = 500,
                                    message = "Reorder failed") })
    @RequestMapping(value = "/reorder/{first}/{second}",
            method = RequestMethod.PUT)
    public ResponseEntity reorder(@PathVariable int first,
                                  @PathVariable int second)
    {
        try
        {
            authenticatorsRegistry.reorder(first, second);
            return ResponseEntity.ok("Reorder succeeded");
        }
        catch (Exception e)
        {
            logger.error("Error during reorder processing.", e);
            return toError("Error during reorder processing: " + e.getLocalizedMessage());
        }
    }

    @ApiOperation(value = "Reloads authenticators registry")
    @ApiResponses(value = { @ApiResponse(code = 200,
            message = "Reload succeeded"),
                            @ApiResponse(code = 500,
                                    message = "Reload failed") })
    @RequestMapping(value = "/reload",
            method = RequestMethod.PUT)
    public ResponseEntity reload()
    {
        try
        {
            authenticatorsScanner.scanAndReloadRegistry();

            return ResponseEntity.ok("Reload succeeded");
        }
        catch (Exception e)
        {
            logger.error("Error during reload processing.", e);
            return toError("Error during reload processing: " + e.getLocalizedMessage());
        }
    }
}
