package org.carlspring.strongbox.rest;

import org.carlspring.logging.rest.AbstractLoggingManagementRestlet;

import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
@Path("/logging")
@Api(value = "/logging")
@PreAuthorize("hasAuthority('ROOT')")
public class LoggingManagementRestlet extends AbstractLoggingManagementRestlet
{


    @ApiOperation(value = "Used to add a logger.", position = 0)
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The logger was added successfully."),
                            @ApiResponse(code = 500, message = "Failed to add logger!") })
    @Override
    public Response addLogger(@ApiParam(value = "The package to log", required = true)
                              String loggerPackage,
                              @ApiParam(value = "The logging level", required = true)
                              String level,
                              @ApiParam(value = "The name of the appender", required = true)
                              String appenderName)
    {
        return super.addLogger(loggerPackage, level, appenderName);
    }

    @ApiOperation(value = "Used to update a logger.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The logger was updated successfully."),
                            @ApiResponse(code = 400, message = "Failed to update logger!"),
                            @ApiResponse(code = 404, message = "Logger '${loggerPackage}' not found.") })
    @Override
    public Response updateLogger(@ApiParam(value = "The package to log", required = true)
                                 String loggerPackage,
                                 @ApiParam(value = "The logging level", required = true)
                                 String level)
    {
        return super.updateLogger(loggerPackage, level);
    }

    @ApiOperation(value = "Used to delete a logger.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "The logger was deleted successfully."),
                            @ApiResponse(code = 400, message = "Failed to delete the logger!"),
                            @ApiResponse(code = 404, message = "Logger '${loggerPackage}' not found.") })
    @Override
    public Response deleteLogger(@ApiParam(value = "The logger to delete", required = true)
                                 String loggerPackage) throws IOException
    {
        return super.deleteLogger(loggerPackage);
    }

    @ApiOperation(value = "Used to download a log file.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "", response = String.class),
                            @ApiResponse(code = 400, message = "Failed to resolve the log!") })
    @Override
    public Response downloadLog(@ApiParam(value = "The relative path to the log file", required = true)
                                String path)
    {
        return super.downloadLog(path);
    }

    @ApiOperation(value = "Used to download the Logback configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = ""),
                            @ApiResponse(code = 400, message = "Failed to resolve the logging configuration!")})
    @Override
    public Response downloadLogbackConfiguration()
    {
        return super.downloadLogbackConfiguration();
    }

    @ApiOperation(value = "Used to upload and re-load a Logback configuration file.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Logback configuration uploaded successfully."),
                            @ApiResponse(code = 400, message = "Failed to resolve the logging configuration!")  })
    @Override
    public Response uploadLogbackConfiguration(@ApiParam(value = "The input stream of the the Logback configuration file to load", required = true)
                                               InputStream is)
    {
        return super.uploadLogbackConfiguration(is);
    }

}
