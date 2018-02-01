package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.controllers.support.*;
import org.carlspring.strongbox.resource.ResourceCloser;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.pool.PoolStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Provides common subroutines that will be useful for any backend controllers.
 *
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 */
public abstract class BaseController
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    protected ConfigurationManager configurationManager;

    protected Object getResponseEntityBody(String message, String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return new ResponseEntityBody(message);
        }
        else
        {
            return message;
        }
    }

    protected ResponseEntityBody getResponseEntityBody(String message)
    {
        return new ResponseEntityBody(message);
    }

    protected ResponseEntity toResponseEntity(String message,
                                              HttpStatus httpStatus)
    {
        return ResponseEntity.status(httpStatus)
                             .body(getResponseEntityBody(message));
    }

    protected Object getPortEntityBody(int port, String accept)
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

    protected Object getBaseUrlEntityBody(String baseUrl, String accept)
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

    protected Object getNumberOfConnectionsEntityBody(int numberOfConnections, String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return new NumberOfConnectionsEntityBody(numberOfConnections);
        }
        else
        {
            return String.valueOf(numberOfConnections);
        }
    }

    protected Object getPoolStatsEntityBody(PoolStats poolStats, String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return new PoolStatsEntityBody(poolStats);
        }
        else
        {
            return String.valueOf(poolStats);
        }
    }

    protected ResponseEntity toResponseEntityError(String message,
                                                   HttpStatus httpStatus)
    {
        return ResponseEntity.status(httpStatus)
                             .body(new ErrorResponseEntityBody(message));
    }

    protected ResponseEntity toResponseEntityError(String message)
    {
        return toResponseEntityError(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected ResponseEntity toError(String message,
                                     Boolean... wrapBody)
    {
        if (wrapBody != null)
        {
            return toError(new RuntimeException(message), wrapBody);
        }

        return toError(new RuntimeException(message));
    }

    protected ResponseEntity toError(Throwable cause,
                                     Boolean... wrapBody)
    {
        logger.error(cause.getMessage(), cause);
        Object bodyContent = wrapBody != null ? getResponseEntityBody(cause.getMessage()) : cause.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(bodyContent);
    }

    protected Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    protected void copyToResponse(InputStream is,
                                  HttpServletResponse response)
            throws Exception
    {
        OutputStream os = response.getOutputStream();

        try
        {
            long totalBytes = 0L;

            int readLength;
            byte[] bytes = new byte[4096];
            while ((readLength = is.read(bytes, 0, bytes.length)) != -1)
            {
                // Write the artifact
                os.write(bytes, 0, readLength);
                os.flush();

                totalBytes += readLength;
            }

            response.setHeader("Content-Length", Long.toString(totalBytes));
            response.flushBuffer();
        }
        finally
        {
            ResourceCloser.close(is, logger);
            ResourceCloser.close(os, logger);
        }
    }

}
