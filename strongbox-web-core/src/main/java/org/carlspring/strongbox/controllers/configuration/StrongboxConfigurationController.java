package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.support.ConfigurationException;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Pablo Tirado
 */
@Controller
@RequestMapping("/api/configuration/strongbox/xml")
@Api(value = "/api/configuration/strongbox/xml")
public class StrongboxConfigurationController
        extends BaseController
{

    private static final Logger logger = LoggerFactory.getLogger(StrongboxConfigurationController.class);

    private final ConfigurationManagementService configurationManagementService;

    public StrongboxConfigurationController(ConfigurationManagementService configurationManagementService)
    {
        this.configurationManagementService = configurationManagementService;
    }


    @ApiOperation(value = "Upload a strongbox.xml and reload the server's configuration.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The configuration was updated successfully."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_UPLOAD')")
    @RequestMapping(value = "",
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

            return ResponseEntity.ok("The configuration was updated successfully.");
        }
        catch (ConfigurationException e)
        {
            logger.error(e.getMessage(), e);

            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Retrieves the strongbox.xml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = ""),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW')")
    @RequestMapping(value = "",
                    method = RequestMethod.GET,
                    produces = { MediaType.APPLICATION_XML_VALUE,
                                 MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getConfigurationXML()
    {
        logger.debug("Received configuration request.");

        return ResponseEntity.status(HttpStatus.OK)
                             .body(getConfiguration());
    }
}
