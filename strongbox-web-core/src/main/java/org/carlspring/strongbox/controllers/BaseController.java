package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.controllers.support.ErrorResponseEntityBody;
import org.carlspring.strongbox.controllers.support.ResponseEntityBody;
import org.carlspring.strongbox.resource.ResourceCloser;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
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

    protected Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

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

    protected Object getListResponseEntityBody(List<?> list, String accept)
    {
        if (MediaType.APPLICATION_JSON_VALUE.equals(accept))
        {
            return list;
        }
        else
        {
            return String.valueOf(list);
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
                                     String... accept)
    {
        if (accept != null && MediaType.APPLICATION_JSON_VALUE.equals(accept[0]))
        {
            return toError(new RuntimeException(message), accept);
        }

        return toError(new RuntimeException(message));
    }

    protected ResponseEntity toError(Throwable cause,
                                     String... accept)
    {
        logger.error(cause.getMessage(), cause);
        Object bodyContent = accept != null ? getResponseEntityBody(cause.getMessage(), accept[0]) : cause.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(bodyContent);
    }

    protected void copyToResponse(InputStream is,
                                  HttpServletResponse response)
            throws IOException
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

            response.setHeader(HttpHeaders.CONTENT_LENGTH, Long.toString(totalBytes));
            response.flushBuffer();
        }
        finally
        {
            ResourceCloser.close(is, logger);
            ResourceCloser.close(os, logger);
        }
    }

}
