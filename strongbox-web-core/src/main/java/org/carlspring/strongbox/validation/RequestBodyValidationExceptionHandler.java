package org.carlspring.strongbox.validation;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author Przemyslaw Fusik
 */
@ControllerAdvice
public class RequestBodyValidationExceptionHandler
        extends ResponseEntityExceptionHandler
{

    @ExceptionHandler(RequestBodyValidationException.class)
    protected ResponseEntity<?> handleRequestBodyValidationException(final RequestBodyValidationException ex,
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
}
