package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.configuration.MutableConfiguration;
import org.carlspring.strongbox.controllers.ResponseMessage;
import org.carlspring.strongbox.services.ConfigurationManagementService;

import io.swagger.annotations.*;

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
@RequestMapping("/api/configuration/strongbox")
@Api(value = "/api/configuration/strongbox")
public class StrongboxConfigurationController
        extends BaseConfigurationController
{

    public StrongboxConfigurationController(ConfigurationManagementService configurationManagementService)
    {
        super(configurationManagementService);
    }

    @ApiOperation(value = "Upload a strongbox.xml and reload the server's configuration.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The configuration was updated successfully."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_UPLOAD')")
    @RequestMapping(method = RequestMethod.PUT,
                    produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
                    consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<ResponseMessage> setStrongboxConfiguration(@ApiParam(value = "The strongbox.xml configuration file", required = true) @RequestBody MutableConfiguration configuration)
    {
        configurationManagementService.setConfiguration(configuration);

        logger.info("Received new configuration over REST.");

        return new ResponseEntity<>(ResponseMessage.empty().withMessage("The configuration was updated successfully."), HttpStatus.OK);
    }

    @ApiOperation(value = "Retrieves the strongbox.xml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = ""),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW')")
    @RequestMapping(method = RequestMethod.GET,
                    produces = { MediaType.APPLICATION_XML_VALUE,
                                 MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<MutableConfiguration> getStrongboxConfiguration()
    {
        logger.debug("Retrieved strongbox.xml configuration file.");

        return new ResponseEntity<>(getMutableConfigurationClone(), HttpStatus.OK);
    }
}
