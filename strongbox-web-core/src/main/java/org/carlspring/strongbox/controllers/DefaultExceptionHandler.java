package org.carlspring.strongbox.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.carlspring.strongbox.controllers.support.ErrorResponseEntityBody;
import org.carlspring.strongbox.data.criteria.QueryParserException;
import org.carlspring.strongbox.validation.RequestBodyValidationError;
import org.carlspring.strongbox.validation.RequestBodyValidationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

@ControllerAdvice
public class DefaultExceptionHandler extends ResponseEntityExceptionHandler
{

    @Inject
    private ContentNegotiationManager contentNegotiationManager;

    @ExceptionHandler({ QueryParserException.class })
    protected ResponseEntity<?> handleRequestParseException(Exception ex,
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
        if (requestedContent(request).equals(MediaType.TEXT_PLAIN))
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

    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                             @Nullable Object body,
                                                             HttpHeaders headers,
                                                             HttpStatus status,
                                                             WebRequest request)
    {
        return provideDefaultErrorResponse(ex, request, status);
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

    private ResponseEntity<Object> provideDefaultErrorResponse(Exception ex,
                                                               WebRequest request,
                                                               HttpStatus status)
    {
        MediaType contentType = requestedContent(request);

        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, contentType.toString());

        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status))
        {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }

        if (contentType.equals(MediaType.TEXT_PLAIN))
        {
            return new ResponseEntity<>(ex.getMessage(), headers, status);
        }

        return new ResponseEntity<>(new ErrorResponseEntityBody(ex.getMessage()), headers, status);
    }

    private MediaType requestedContent(WebRequest request)
    {
        List<MediaType> mediaTypes = new ArrayList<>();
        try
        {
            mediaTypes.addAll(contentNegotiationManager.resolveMediaTypes((NativeWebRequest) request));
        }
        catch (HttpMediaTypeNotAcceptableException e1)
        {
            logger.error(String.format("Reuqested invalid content-type [%s]", request.getHeader(HttpHeaders.ACCEPT)));
            mediaTypes.add(MediaType.APPLICATION_JSON);
        }

        MediaType result = mediaTypes.stream()
                                     .reduce(null, this::reduceByPriority);
        
        return Optional.ofNullable(result).orElse(MediaType.APPLICATION_JSON);
    }

    private MediaType reduceByPriority(MediaType m1,
                                       MediaType m2)
    {
        if (MediaType.APPLICATION_JSON.equals(m1) || MediaType.APPLICATION_JSON.equals(m2))
        {
            return MediaType.APPLICATION_JSON;
        }
        if (MediaType.APPLICATION_XML.equals(m1) || MediaType.APPLICATION_XML.equals(m2))
        {
            return MediaType.APPLICATION_XML;
        }
        if (MediaType.TEXT_PLAIN.equals(m1) || MediaType.TEXT_PLAIN.equals(m2))
        {
            return MediaType.TEXT_PLAIN;
        }
        return null;
    }

}
