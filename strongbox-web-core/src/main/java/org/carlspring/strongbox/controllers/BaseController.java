package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.controllers.support.ErrorResponseEntityBody;
import org.carlspring.strongbox.controllers.support.ListEntityBody;
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

    /**
     * You will rarely need to use this method directly. In most cases you should consider using the methods below
     * to guarantee consistency in the returned responses to the client request.
     *
     * @param message       Message to be returned to the client.
     * @param acceptHeader  The Accept header, so that we can return the proper json/plain text response.
     *
     * @return Object
     */
    protected Object getResponseEntityBody(String message, String acceptHeader)
    {
        if (MediaType.TEXT_PLAIN_VALUE.equals(acceptHeader))
        {
            return message;
        }
        else
        {
            return new ResponseEntityBody(message);
        }
    }

    /**
     * @param fieldName JSON field name
     * @param list      the list
     *
     * @return
     */
    protected ResponseEntity getJSONListResponseEntityBody(String fieldName, List<?> list)
    {
        return ResponseEntity.ok(new ListEntityBody(fieldName, list));
    }

    /**
     * Used for operations which have been successfully performed.
     *
     * @param message       Success to be returned to the client.
     * @param acceptHeader  The Accept header, so that we can return the proper json/plain text response.
     *
     * @return ResponseEntity
     */
    protected ResponseEntity getSuccessfulResponseEntity(String message, String acceptHeader)
    {
        return ResponseEntity.ok(getResponseEntityBody(message, acceptHeader));
    }

    /**
     * Used for operations which have failed.
     *
     * @param status        Status code to be returned (i.e. 400 Bad Request)
     * @param message       Error to be returned to the client.
     * @param acceptHeader  The Accept header, so that we can return the proper json/plain text response.
     *
     * @return ResponseEntity
     */
    protected ResponseEntity getFailedResponseEntity(HttpStatus status, String message, String acceptHeader)
    {
        return ResponseEntity.status(status)
                             .body(getResponseEntityBody(message, acceptHeader));
    }

    /**
     * @param message       Error to be returned to the client.
     * @param acceptHeader  The Accept header, so that we can return the proper json/plain text response.
     *
     * @return ResponseEntity
     */
    protected ResponseEntity getBadRequestResponseEntity(String message, String acceptHeader)
    {
        return getFailedResponseEntity(HttpStatus.BAD_REQUEST, message, acceptHeader);
    }

    /**
     * Used in cases where resource could not be found.
     *
     * @param acceptHeader  The Accept header, so that we can return the proper json/plain text response.
     *
     * @return ResponseEntity
     */
    protected ResponseEntity getNotFoundResponseEntity(String acceptHeader)
    {
        return getNotFoundResponseEntity("Resource Not Found", acceptHeader);

    }

    /**
     * Used in cases where resource could not be found.
     *
     * @param message       Error to be returned to the client.
     * @param acceptHeader  The Accept header, so that we can return the proper json/plain text response.
     *
     * @return ResponseEntity
     */
    protected ResponseEntity getNotFoundResponseEntity(String message, String acceptHeader)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                             .body(getResponseEntityBody(message, acceptHeader));

    }

    /**
     * Returns an Internal Server Error response.
     *
     * Should only be used for cases when it's really unclear what might have gone wrong
     * and that's truly the only reasonable response.
     *
     * @return ResponseEntity
     */
    protected ResponseEntity getInternalServerErrorResponseEntity(String acceptHeader) {
        return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,
                                          new RuntimeException("500 Internal Server Error"),
                                          acceptHeader);
    }

    /**
     * Returns an
     *
     * @param message       Error to be returned to the client.
     * @param acceptHeader  The Accept header, so that we can return the proper json/plain text response.
     *
     * @return ResponseEntity
     */
    protected ResponseEntity getRuntimeExceptionResponseEntity(String message, String acceptHeader)
    {
        return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR,
                                          new RuntimeException(message),
                                          acceptHeader);
    }

    /**
     * @param httpStatus    HttpStatus to be returned.
     * @param cause         Exception.
     * @param acceptHeader  The Accept header, so that we can return the proper json/plain text response.
     *
     * @return ResponseEntity
     */
    protected ResponseEntity getExceptionResponseEntity(HttpStatus httpStatus, Throwable cause, String acceptHeader)
    {
        logger.error(cause.getMessage(), cause);

        Object responseEntityBody = getResponseEntityBody(cause.getMessage(), acceptHeader);
        return ResponseEntity.status(httpStatus)
                             .body(responseEntityBody);
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
