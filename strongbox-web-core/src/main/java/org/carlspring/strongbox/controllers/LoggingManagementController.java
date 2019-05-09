package org.carlspring.strongbox.controllers;

import org.carlspring.logging.exceptions.AppenderNotFoundException;
import org.carlspring.logging.exceptions.LoggerNotFoundException;
import org.carlspring.logging.exceptions.LoggingConfigurationException;
import org.carlspring.logging.services.LoggingManagementService;
import org.carlspring.strongbox.booters.PropertiesBooter;
import org.carlspring.strongbox.domain.DirectoryListing;
import org.carlspring.strongbox.services.DirectoryListingService;
import org.carlspring.strongbox.services.DirectoryListingServiceImpl;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import io.swagger.annotations.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import static org.carlspring.strongbox.controllers.LoggingManagementController.ROOT_CONTEXT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * This controllers provides a simple wrapper over REST API for the LoggingManagementService.
 *
 * @author Martin Todorov
 * @author Pablo Tirado
 * @author Aditya Srinivasan
 */
@Controller
@Api(value = ROOT_CONTEXT)
@RequestMapping(ROOT_CONTEXT)
public class LoggingManagementController
        extends BaseController
{

    public final static String ROOT_CONTEXT = "/api/logging";

    @Inject
    private PropertiesBooter propertiesBooter;

    @Inject
    private LoggingManagementService loggingManagementService;

    private DirectoryListingService directoryListingService;
    
    public DirectoryListingService getDirectoryListingService()
    {
        return Optional.ofNullable(directoryListingService).orElseGet(() -> {
            String baseUrl = StringUtils.chomp(configurationManager.getConfiguration().getBaseUrl(), "/");

            return directoryListingService = new DirectoryListingServiceImpl(String.format("%s/api/logging", baseUrl));
        });
    }
    
    @ApiOperation(value = "Used to add new logger.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The logger was added successfully."),
                            @ApiResponse(code = 400, message = "Could not add a new logger.") })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_ADD_LOGGER','CONFIGURE_LOGS')")
    @PutMapping(value = "/logger",
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity addLogger(@ApiParam(value = "The logger name", required = true)
                                    @RequestParam("logger") String loggerPackage,
                                    @ApiParam(value = "The logger level", required = true)
                                    @RequestParam("level") String level,
                                    @ApiParam(value = "The logger appender name", required = true)
                                    @RequestParam("appenderName") String appenderName,
                                    @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        try
        {
            loggingManagementService.addLogger(loggerPackage, level, appenderName);

            return getSuccessfulResponseEntity("The logger was added successfully.", accept);
        }
        catch (LoggingConfigurationException | AppenderNotFoundException e)
        {
            String message = "Could not add a new logger.";

            return getExceptionResponseEntity(BAD_REQUEST, message, e, accept);
        }
    }

    @ApiOperation(value = "Used to update existing logger.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The logger was updated successfully."),
                            @ApiResponse(code = 400, message = "Could not update logger."),
                            @ApiResponse(code = 404, message = "Logger was not found.") })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_UPDATE_LOGGER','CONFIGURE_LOGS')")
    @PostMapping(value = "/logger",
                 produces = { MediaType.TEXT_PLAIN_VALUE,
                              MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity updateLogger(@ApiParam(value = "The logger name", required = true)
                                       @RequestParam("logger") String loggerPackage,
                                       @ApiParam(value = "The logger level", required = true)
                                       @RequestParam("level") String level,
                                       @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        try
        {
            loggingManagementService.updateLogger(loggerPackage, level);

            return getSuccessfulResponseEntity("The logger was updated successfully.", accept);
        }
        catch (LoggingConfigurationException e)
        {
            String message = "Could not update logger.";

            return getExceptionResponseEntity(BAD_REQUEST, message, e, accept);
        }
        catch (LoggerNotFoundException e)
        {
            String message = "Logger '" + loggerPackage + "' not found!";
            return getNotFoundResponseEntity(message, accept);
        }
    }

    @ApiOperation(value = "Used to delete existing logger.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The logger was deleted successfully."),
                            @ApiResponse(code = 400, message = "Could not delete the logger."),
                            @ApiResponse(code = 404, message = "Logger was not found.") })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_DELETE_LOGGER','CONFIGURE_LOGS')")
    @DeleteMapping(value = "/logger",
                   produces = { MediaType.TEXT_PLAIN_VALUE,
                                MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity deleteLogger(@ApiParam(value = "The logger name", required = true)
                                       @RequestParam("logger") String loggerPackage,
                                       @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        try
        {
            loggingManagementService.deleteLogger(loggerPackage);

            return getSuccessfulResponseEntity("The logger was deleted successfully.", accept);
        }
        catch (LoggingConfigurationException e)
        {
            String message = "Could not delete the logger.";

            logger.error(message, e);

            return ResponseEntity.status(BAD_REQUEST).body(getResponseEntityBody(message, accept));
        }
        catch (LoggerNotFoundException e)
        {
            String message = "Logger '" + loggerPackage + "' not found!";

            return getNotFoundResponseEntity(message, accept);
        }
    }

    @ApiOperation(value = "Used to download log data.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The logger was retrieved successfully."),
                            @ApiResponse(code = 400, message = "Could not download log data.") })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_RETRIEVE_LOG','CONFIGURE_LOGS')")
    @GetMapping(value = "/log/{path}", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity downloadLog(@PathVariable String path,
                                      @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        try
        {
            logger.debug("Received a request to retrieve log file {}.", path);

            return getStreamToResponseEntity(loggingManagementService.downloadLog(path),
                                             FilenameUtils.getName(path));
        }
        catch (LoggingConfigurationException e)
        {
            String message = "Could not download log data.";

            return getExceptionResponseEntity(BAD_REQUEST, message, e, accept);
        }
    }

    @ApiOperation(value = "Used to download logback configuration.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The logger configuration was retrieved successfully."),
                            @ApiResponse(code = 400, message = "Could not download logback configuration.") })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_RETRIEVE_LOGBACK_CFG','CONFIGURE_LOGS')")
    @GetMapping(value = "/logback", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity downloadLogbackConfiguration(@RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        try
        {
            return getStreamToResponseEntity(loggingManagementService.downloadLogbackConfiguration(),
                                             "strongbox-logback-configuration.xml");
        }
        catch (LoggingConfigurationException e)
        {
            String message = "Could not download logback configuration.";

            return getExceptionResponseEntity(BAD_REQUEST, message, e, accept);
        }
    }

    @ApiOperation(value = "Used to upload logback configuration.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The logger configuration was uploaded successfully."),
                            @ApiResponse(code = 400, message = "An error occurred.") })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_UPLOAD_LOGBACK_CFG','CONFIGURE_LOGS')")
    @PostMapping(value = "/logback",
                 consumes = APPLICATION_XML_VALUE,
                 produces = { MediaType.TEXT_PLAIN_VALUE,
                              MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity uploadLogbackConfiguration(HttpServletRequest request,
                                                     @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        try
        {
            loggingManagementService.uploadLogbackConfiguration(request.getInputStream());

            return getSuccessfulResponseEntity("Logback configuration uploaded successfully.", accept);
        }
        catch (IOException | LoggingConfigurationException e)
        {
            String message = "Could not upload logback configuration.";
            return getExceptionResponseEntity(BAD_REQUEST, message, e, accept);
        }
    }

    @ApiOperation(value = "Used to get log directory.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The log directory was retrieved successfully."),
                            @ApiResponse(code = 500, message = "Server error.") })
    @PreAuthorize("hasAuthority('VIEW_LOGS')")
    @GetMapping(value = { "/logs/{urlPath:.+}" },
                produces = {MediaType.TEXT_PLAIN_VALUE, MediaType.TEXT_HTML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Object generateLogDirectoryListing(@PathVariable("urlPath") Optional<String> rawPath,
                                              ModelMap model,
                                              HttpServletRequest request,
                                              @RequestHeader(value = HttpHeaders.ACCEPT,
                                                             required = false) String acceptHeader)
    {
        logger.debug("Requested directory listing of logs " + ROOT_CONTEXT + "/logs/{}", rawPath.orElse(""));

        try
        {
            Path logsBaseDir = Paths.get(propertiesBooter.getVaultDirectory(), "/logs/");
            Path requestedLogPath = Paths.get(logsBaseDir.toString(), rawPath.orElse(""));

            if(Files.exists(requestedLogPath) && !Files.isDirectory(requestedLogPath))
            {
                return getBadRequestResponseEntity("Requested path is not a directory!", acceptHeader);
            }

            DirectoryListing directoryListing = getDirectoryListingService().fromPath(logsBaseDir, requestedLogPath);

            if (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE))
            {
                return ResponseEntity.ok(objectMapper.writer().writeValueAsString(directoryListing));
            }

            String currentUrl = StringUtils.chomp(request.getRequestURI(), "/");
            String downloadUrl = currentUrl.replaceFirst("/logs", "/log");

            model.addAttribute("showBack", false);
            model.addAttribute("currentUrl", currentUrl);
            model.addAttribute("downloadBaseUrl", downloadUrl);
            model.addAttribute("directories", directoryListing.getDirectories());
            model.addAttribute("files", directoryListing.getFiles());

            return new ModelAndView("directoryListing", model);
        }
        catch (Exception e)
        {
            String message = "Attempt to browse logs failed. Check server logs for more information.";
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message, e, acceptHeader);
        }
    }

}
