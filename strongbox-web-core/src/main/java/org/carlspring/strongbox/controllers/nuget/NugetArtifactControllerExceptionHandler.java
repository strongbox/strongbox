package org.carlspring.strongbox.controllers.nuget;

import org.carlspring.strongbox.controllers.support.ErrorResponseEntityBody;
import org.carlspring.strongbox.nuget.filter.NugetODataFilterParserException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class NugetArtifactControllerExceptionHandler extends ResponseEntityExceptionHandler
{

    @ExceptionHandler(NugetODataFilterParserException.class)
    protected ResponseEntity<?> handleRepositoryRelativePathConstructionException(final NugetODataFilterParserException ex,
                                                                                  final WebRequest request)
    {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        return handleExceptionInternal(ex,
                                       new ErrorResponseEntityBody(ex.getMessage()),
                                       headers,
                                       HttpStatus.BAD_REQUEST, request);
    }

}
