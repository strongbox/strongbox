package org.carlspring.strongbox.controllers;

import org.carlspring.logging.exceptions.AppenderNotFoundException;
import org.carlspring.logging.exceptions.LoggerNotFoundException;
import org.carlspring.logging.exceptions.LoggingConfigurationException;
import org.carlspring.logging.services.LoggingManagementService;
import org.carlspring.strongbox.data.PropertyUtils;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
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
@Api(value = "/api/logging")
@RequestMapping("/api/logging")
public class LoggingManagementController
        extends BaseController
{

    @Inject
    private LoggingManagementService loggingManagementService;

    @ApiOperation(value = "Used to add new logger.",
                  position = 0)
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

    @ApiOperation(value = "Used to update existing logger.",
                  position = 0)
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
            return getExceptionResponseEntity(NOT_FOUND, message, e, accept);
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

            return getExceptionResponseEntity(NOT_FOUND, message, e, accept);
        }
    }

    @ApiOperation(value = "Used to download log data.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The logger was retrieved successfully."),
                            @ApiResponse(code = 400, message = "Could not download log data.") })
    @PreAuthorize("hasAnyAuthority('CONFIGURATION_RETRIEVE_LOG','CONFIGURE_LOGS')")
    @GetMapping(value = "/log/{path}",
                produces = TEXT_PLAIN_VALUE)
    public ResponseEntity downloadLog(@PathVariable String path,
                                      @RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws Exception
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
    @GetMapping(value = "/logback",
                produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity downloadLogbackConfiguration(@RequestHeader(HttpHeaders.ACCEPT) String accept)
            throws Exception
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
                            @ApiResponse(code = 400, message = "Could not download log directory.") })
    @PreAuthorize("hasAuthority('VIEW_LOGS')")
    @GetMapping(value = { "/logs/{urlPath:.+}" },
                produces = TEXT_PLAIN_VALUE)
    public void generateLogDirectoryListing(@PathVariable("urlPath") Optional<String> urlPath,
                                            HttpServletRequest request,
                                            HttpServletResponse response)
            throws IOException
    {
        String requestUriString = request.getRequestURI();

        String uriLogDirPath = urlPath.map(s -> "/logs/" + s)
                                      .orElse("/logs/");
        
        Path localLogDirPath = Paths.get(PropertyUtils.getVaultDirectory(), uriLogDirPath);
        
        if (Files.notExists(localLogDirPath))
        {
            response.sendError(404, "File " + localLogDirPath.toString() + " does not exist.");
            
            return;
        }
        
        //Sends a redirect if the URI does not end with a "/"
        if (!requestUriString.endsWith("/"))
        {
            try
            {
                response.sendRedirect(requestUriString + "/");
            }
            catch (IOException e)
            {
                logger.debug("Error redirecting to " + requestUriString + "/");
            }
            return;
        }
        
        try
        {
            logger.debug(" browsing: " + localLogDirPath.toString());
            
            //Generating the HTML View
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append("<head>");
            sb.append(
                    "<style>body{font-family: \"Trebuchet MS\", verdana, lucida, arial, helvetica, sans-serif;} table tr {text-align: left;}</style>");
            sb.append("<title>Index of " + uriLogDirPath + "</title>");
            sb.append("</head>");
            sb.append("<body>");
            sb.append("<h1>Index of " + uriLogDirPath + "</h1>");
            sb.append("<table cellspacing=\"10\">");
            sb.append("<tr>");
            sb.append("<th>Name</th>");
            sb.append("<th>Last modified</th>");
            sb.append("<th>Size</th>");
            sb.append("<th>Description</th>");
            sb.append("</tr>");
            if (!requestUriString.equalsIgnoreCase("/logging/logs/"))
            {
                sb.append("<tr>");
                sb.append("<td colspan=4><a href=\"..\">..</a></td>");
                sb.append("</tr>");
            }
            
            //Adds the Files and folders to the HTML body
            final String localLogFilePath = requestUriString.replace("logs", "log");
            final String localLogDirectoryPath = requestUriString;
            Files.list(localLogDirPath)
                 .sorted(Comparator.comparing(Path::getFileName))
                 .collect(Collectors.toList())
                 .forEach(path -> {
                     try
                     {
                         if (Files.isDirectory(path))
                         {
                             appendFile(sb, path, localLogDirectoryPath);
                         }
                         else
                         {
                             appendFile(sb, path, localLogFilePath);
                         }
                     }
                     catch (Exception e)
                     {
                         logger.error(e.getMessage(), e);
                     }
                 });
            
            sb.append("</table>");
            sb.append("</body>");
            sb.append("</html>");
            
            response.setContentType("text/html;charset=UTF-8");
            response.setStatus(HttpStatus.OK.value());
            response.getWriter().write(sb.toString());
            response.getWriter().flush();
            response.getWriter().close();
        }
        catch (Exception e)
        {
            logger.error(" error accessing requested directory: " + localLogDirPath.getFileName(), e);
            
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    
    protected static boolean appendFile(StringBuilder sb,
                                        Path childFile,
                                        final String requestURL)
            throws IOException
    {
        String name = childFile.toString();
        if (name.startsWith(".") || Files.isHidden(childFile))
        {
            return false;
        }

        String lastModified = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss").format(
                Files.getLastModifiedTime(childFile).toMillis());
        boolean isDirectory = Files.isDirectory(childFile);

        sb.append("<tr>");
        sb.append("<td><a href=\"" + requestURL + URLEncoder.encode(name, "UTF-8") +
                  (isDirectory ? "/" : "") + "\">" + name + (isDirectory ? "/" : "") +
                  "</a></td>");
        sb.append("<td>" + lastModified + "</td>");
        sb.append("<td>" + FileUtils.byteCountToDisplaySize(Files.size(childFile)) + "</td>");
        sb.append("<td></td>");
        sb.append("</tr>");

        return true;
    }
    
}
