package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.controllers.support.ErrorResponseEntityBody;
import org.carlspring.strongbox.data.criteria.QueryParserException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class CommonControllerExceptionHandler extends ResponseEntityExceptionHandler
{

    @ExceptionHandler(QueryParserException.class)
    protected ResponseEntity<?> handleQueryParserException(QueryParserException ex,
                                                           WebRequest request)
    {
        String accept = request.getHeader(HttpHeaders.ACCEPT);

        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, accept);

        return handleExceptionInternal(ex,
                                       new ErrorResponseEntityBody(ex.getMessage()),
                                       headers,
                                       HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleUnknownError(Exception ex,
                                                   WebRequest request)
    {
        String accept = request.getHeader(HttpHeaders.ACCEPT);

        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, accept);

        return handleExceptionInternal(ex,
                                       new ErrorResponseEntityBody(ex.getMessage()),
                                       headers,
                                       HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}
