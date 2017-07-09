package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.controllers.support.ErrorXmlHandler;
import org.carlspring.strongbox.xml.parsers.GenericParser;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
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
    protected ObjectMapper objectMapper;

    @Inject
    protected ConfigurationManager configurationManager;

    protected ResponseEntity toXmlError(String message,
                                        HttpStatus httpStatus)
            throws JAXBException
    {
        return ResponseEntity.status(httpStatus)
                             .body(new GenericParser<>(ErrorXmlHandler.class).serialize(new ErrorXmlHandler(message)));
    }

    protected ResponseEntity toXmlError(String message)
            throws JAXBException
    {
        return toXmlError(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    protected ResponseEntity toError(String message)
    {
        return toError(new RuntimeException(message));
    }

    protected ResponseEntity toError(Throwable cause)
    {
        logger.error(cause.getMessage(), cause);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(cause.getMessage());
    }

    protected Configuration getConfiguration()
    {
        return configurationManager.getConfiguration();
    }

    protected void copyToResponse(InputStream inputStream,
                                  HttpServletResponse response)
            throws Exception
    {
        try
        {
            long totalBytes = ByteStreams.copy(new BufferedInputStream(inputStream), response.getOutputStream());
            response.setHeader("Content-Length", totalBytes + "");
            response.flushBuffer();
            inputStream.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Unable copy to response", e);
        }
    }

}
