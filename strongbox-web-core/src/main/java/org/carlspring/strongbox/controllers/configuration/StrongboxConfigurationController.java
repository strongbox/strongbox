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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.carlspring.strongbox.net.MediaType.APPLICATION_YAML_VALUE;

import java.io.IOException;

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

    @ApiOperation(value = "Upload a strongbox.yaml and reload the server's configuration.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The configuration was updated successfully."),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_UPLOAD')")
    @PutMapping(produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE,
                             APPLICATION_YAML_VALUE },
            consumes = { MediaType.APPLICATION_JSON_VALUE,
                         APPLICATION_YAML_VALUE })
    public ResponseEntity<ResponseMessage> setStrongboxConfiguration(
            @ApiParam(value = "The strongbox.yaml configuration file", required = true) @RequestBody
                    MutableConfiguration configuration) throws IOException
    {
        configurationManagementService.setConfiguration(configuration);

        logger.info("Received new configuration over REST.");

        return new ResponseEntity<>(ResponseMessage.empty().withMessage("The configuration was updated successfully."),
                                    HttpStatus.OK);
    }

    @ApiOperation(value = "Retrieves the strongbox.yaml configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 500, message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW')")
    @GetMapping(produces = { APPLICATION_YAML_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<MutableConfiguration> getStrongboxConfiguration()
    {
        logger.debug("Retrieved strongbox.yaml configuration file.");

        return new ResponseEntity<>(getMutableConfigurationClone(), HttpStatus.OK);
    }

}
