package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.controllers.support.BaseUrlEntityBody;
import org.carlspring.strongbox.controllers.support.ErrorResponseEntityBody;
import org.carlspring.strongbox.controllers.support.PortEntityBody;
import org.carlspring.strongbox.controllers.support.ResponseEntityBody;
import org.carlspring.strongbox.resource.ResourceCloser;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Provides common subroutines that will be useful for any backend controllers.
 *
 * @author Alex Oreshkevich
 */
public abstract class BaseController
{

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    protected ConfigurationManager configurationManager;

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

    protected PortEntityBody getPortEntityBody(int port)
    {
        return new PortEntityBody(port);
    }

    protected ResponseEntity toPortEntity(int port,
                                          HttpStatus httpStatus)
    {
        return ResponseEntity.status(httpStatus)
                             .body(getPortEntityBody(port));
    }

    protected BaseUrlEntityBody getBaseUrlEntityBody(String baseUrl)
    {
        return new BaseUrlEntityBody(baseUrl);
    }

    protected ResponseEntity toBaseUrlEntity(String baseUrl,
                                             HttpStatus httpStatus)
    {
        return ResponseEntity.status(httpStatus)
                             .body(getBaseUrlEntityBody(baseUrl));
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
