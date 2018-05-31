package org.carlspring.strongbox.controllers;

import java.util.List;
import java.util.Optional;

import org.carlspring.strongbox.controllers.support.ErrorResponseEntityBody;
import org.carlspring.strongbox.data.criteria.QueryParserException;
import org.carlspring.strongbox.validation.RequestBodyValidationError;
import org.carlspring.strongbox.validation.RequestBodyValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler
{

    @ExceptionHandler(QueryParserException.class)
    protected ResponseEntity<?> handleQueryParserException(QueryParserException ex,
                                                           WebRequest request)
    {
        return provideDefaultErrorResponse(ex, request, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex,
                                                            WebRequest request)
    {
        return provideDefaultErrorResponse(ex, request, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RequestBodyValidationException.class)
    protected ResponseEntity<?> handleRequestBodyValidationException(final RequestBodyValidationException ex,
                                                                     final WebRequest request)
    {
        if (aceptOf(request).contains(MediaType.TEXT_PLAIN_VALUE))
        {
            return provideDefaultErrorResponse(ex, request, HttpStatus.BAD_REQUEST);
        }
        else
        {
            return provideValidationErrorResponse(ex, request);
        }
    }
    
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleUnknownError(Exception ex,
                                                   WebRequest request)
    {
        return provideDefaultErrorResponse(ex, request, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<?> provideValidationErrorResponse(final RequestBodyValidationException ex,
                                                             final WebRequest request)
    {
        final RequestBodyValidationError validationError = new RequestBodyValidationError(ex.getMessage());

        final List<FieldError> fieldErrors = ex.getErrors().getFieldErrors();
        for (final FieldError fieldError : fieldErrors)
        {
            validationError.add(fieldError.getField(), fieldError.getDefaultMessage());
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return handleExceptionInternal(ex, validationError, headers, HttpStatus.BAD_REQUEST, request);
    }

    private ResponseEntity<?> provideDefaultErrorResponse(Exception ex,
                                                          WebRequest request,
                                                          HttpStatus status)
    {
        String accept = aceptOf(request);

        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, accept);

        if (accept.contains(MediaType.TEXT_PLAIN_VALUE))
        {
            return handleExceptionInternal(ex, ex.getMessage() + "\n", headers, HttpStatus.BAD_REQUEST, request);
        }

        return handleExceptionInternal(ex,
                                       new ErrorResponseEntityBody(ex.getMessage()),
                                       headers,
                                       status, request);
    }

    private String aceptOf(WebRequest request)
    {
        return Optional.of(request.getHeader(HttpHeaders.ACCEPT)).orElse(MediaType.APPLICATION_JSON_VALUE);
    }
}
