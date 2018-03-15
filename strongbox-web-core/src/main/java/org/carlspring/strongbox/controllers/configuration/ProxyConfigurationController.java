package org.carlspring.strongbox.controllers.configuration;

import org.carlspring.strongbox.configuration.ProxyConfiguration;
import org.carlspring.strongbox.service.ProxyRepositoryConnectionPoolConfigurationService;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.services.support.ConfigurationException;

import javax.inject.Inject;

import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author Pablo Tirado
 */
@Controller
@RequestMapping("/api/configuration/strongbox/proxy-configuration")
@Api(value = "/api/configuration/strongbox/proxy-configuration")
public class ProxyConfigurationController
        extends BaseConfigurationController
{

    public ProxyConfigurationController(ConfigurationManagementService configurationManagementService)
    {
        super(configurationManagementService);
    }

    @ApiOperation(value = "Updates the proxy configuration for a repository, if one is specified, or, otherwise, the global proxy settings.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = "The proxy configuration was updated successfully."),
                            @ApiResponse(code = 500,
                                         message = "An error occurred.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_SET_GLOBAL_PROXY_CFG')")
    @RequestMapping(value = "",
                    method = RequestMethod.PUT,
                    consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity setProxyConfiguration(@ApiParam(value = "The storageId")
                                                @RequestParam(value = "storageId", required = false) String storageId,
                                                @ApiParam(value = "The repositoryId")
                                                @RequestParam(value = "repositoryId", required = false)
                                                        String repositoryId,
                                                @ApiParam(value = "The proxy configuration for this proxy repository", required = true)
                                                @RequestBody ProxyConfiguration proxyConfiguration)
    {
        logger.debug("Received proxy configuration \n: {}", proxyConfiguration);

        try
        {
            configurationManagementService.setProxyConfiguration(storageId, repositoryId, proxyConfiguration);
            return ResponseEntity.ok("The proxy configuration was updated successfully.");
        }
        catch (ConfigurationException e)
        {
            logger.error(e.getMessage(), e);

            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Returns the proxy configuration for a repository, if one is specified, or, otherwise, the global proxy settings.")
    @ApiResponses(value = { @ApiResponse(code = 200,
                                         message = ""),
                            @ApiResponse(code = 404,
                                         message = "The proxy configuration for '${storageId}:${repositoryId}' was not found.") })
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW_GLOBAL_PROXY_CFG')")
    @RequestMapping(value = "",
                    method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getProxyConfiguration(@ApiParam(value = "The storageId")
                                                @RequestParam(value = "storageId", required = false) String storageId,
                                                @ApiParam(value = "The repositoryId")
                                                @RequestParam(value = "repositoryId", required = false)
                                                        String repositoryId)
    {
        ProxyConfiguration proxyConfiguration;
        if (storageId == null)
        {
            proxyConfiguration = configurationManagementService.getProxyConfiguration();
        }
        else
        {
            proxyConfiguration = configurationManagementService.getStorage(storageId)
                                                               .getRepository(repositoryId)
                                                               .getProxyConfiguration();
        }

        if (proxyConfiguration != null)
        {
            return ResponseEntity.status(HttpStatus.OK)
                                 .body(proxyConfiguration);
        }
        else
        {
            String message = "The proxy configuration" +
                             (storageId != null ? " for " + storageId + ":" + repositoryId : "") +
                             " was not found.";

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(message);
        }
    }
}
