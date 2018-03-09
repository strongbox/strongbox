package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.controllers.support.BaseUrlEntityBody;
import org.carlspring.strongbox.controllers.support.PortEntityBody;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.support.ConfigurationException;

import java.io.IOException;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author Pablo Tirado
 */
@Controller
@RequestMapping("/api/configuration/strongbox")
@Api(value = "/api/configuration/strongbox")
public class ServerConfigurationController
        extends BaseController
{

    private static final Logger logger = LoggerFactory.getLogger(ServerConfigurationController.class);

    private final ConfigurationManagementService configurationManagementService;

    public ServerConfigurationController(ConfigurationManagementService configurationManagementService)
    {
        this.configurationManagementService = configurationManagementService;
    }

    @ApiOperation(value = "Updates the base URL of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The base URL was updated."),
                            @ApiResponse(code = 400,
                                         message = "Could not update the base URL of the service.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_BASE_URL')")
    @PutMapping(value = "/baseUrl",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setBaseUrl(@ApiParam(value = "The base URL", required = true)
                                     @RequestBody BaseUrlEntityBody baseUrlEntity,
                                     @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        try
        {
            String newBaseUrl = baseUrlEntity.getBaseUrl();
            configurationManagementService.setBaseUrl(newBaseUrl);

            logger.info("Set baseUrl to [{}].", newBaseUrl);

            return ResponseEntity.ok(getResponseEntityBody("The base URL was updated.", accept));
        }
        catch (ConfigurationException e)
        {
            String message = "Could not update the base URL of the service.";
            logger.error(message, e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }
    }

    @ApiOperation(value = "Returns the base URL of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "",
                                         response = String.class),
                            @ApiResponse(code = 404,
                                         message = "No value for baseUrl has been defined yet.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_BASE_URL')")
    @GetMapping(value = "/baseUrl",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getBaseUrl(@RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws IOException
    {
        if (configurationManagementService.getBaseUrl() != null)
        {
            return ResponseEntity.ok(getBaseUrlEntityBody(configurationManagementService.getBaseUrl(), accept));
        }
        else
        {
            String message = "No value for baseUrl has been defined yet.";
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(getResponseEntityBody(message, accept));
        }
    }

    @ApiOperation(value = "Sets the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The port was updated."),
                            @ApiResponse(code = 400,
                                         message = "Could not update the strongbox port.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_PORT')")
    @PutMapping(value = "/port/{port}",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setPort(@ApiParam(value = "The port of the service", required = true)
                                  @PathVariable int port,
                                  @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        try
        {
            configurationManagementService.setPort(port);

            logger.info("Set port to {}. This operation will require a server restart.", port);

            return ResponseEntity.ok(getResponseEntityBody("The port was updated.", accept));
        }
        catch (ConfigurationException e)
        {
            String message = "Could not update the strongbox port.";
            logger.error(message, e);

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody(message, accept));
        }
    }

    @ApiOperation(value = "Sets the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The port was updated."),
                            @ApiResponse(code = 400,
                                         message = "Could not update the strongbox port.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_PORT')")
    @PutMapping(value = "/port",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setPort(@ApiParam(value = "The port of the service", required = true)
                                  @RequestBody PortEntityBody portEntity,
                                  @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        return setPort(portEntity.getPort(), accept);
    }

    @ApiOperation(value = "Returns the port of the service.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "",
                                         response = String.class) })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_PORT')")
    @GetMapping(value = "/port",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getPort(@RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        return ResponseEntity.ok(getPortEntityBody(configurationManagementService.getPort(), accept));
    }

    private Object getBaseUrlEntityBody(String baseUrl,
                                        String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return new BaseUrlEntityBody(baseUrl);
        }
        else
        {
            return baseUrl;
        }
    }

    private Object getPortEntityBody(int port,
                                     String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return new PortEntityBody(port);
        }
        else
        {
            return String.valueOf(port);
        }
    }
}
