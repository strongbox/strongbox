package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.controllers.support.ExampleEntityBody;
import org.carlspring.strongbox.forms.ExampleForm;
import org.carlspring.strongbox.validation.RequestBodyValidationException;

import java.util.Arrays;
import java.util.List;

import io.swagger.annotations.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * This oversimplified example controller is written following the How-To-Implement-Spring-Controllers guide.
 * It's purpose is entirely educational and is meant to help newcomers.
 * <p>
 * https://github.com/strongbox/strongbox/wiki/How-To-Implement-Spring-Controllers
 *
 * @author Przemyslaw Fusik
 * @author Steve Todorov
 */
@RestController
@RequestMapping("/example-controller")
@Api(value = "/example-controller")
public class ExampleController
        extends BaseController
{

    public static final String NOT_FOUND_MESSAGE = "Could not find record in database.";


    @ApiOperation(value = "List available examples")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Everything went ok") })
    @GetMapping(value = "/all",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getExamples()
    {
        List<String> list = Arrays.asList("a", "foo", "bar", "list", "of", "strings");
        return getJSONListResponseEntityBody("examples", list);
    }

    @ApiOperation(value = "Show specific example")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Everything went ok") })
    @GetMapping(value = "/get/{example}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getExample(@ApiParam(value = "Get a specific example", required = true)
                                     @PathVariable String example,
                                     @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        if (example.equals("not-found"))
        {
            return getNotFoundResponseEntity(NOT_FOUND_MESSAGE, accept);
        }

        ExampleEntityBody body = new ExampleEntityBody(example);
        return ResponseEntity.ok(body);
    }

    @ApiOperation(value = "Update example's credentials.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Everything went ok"),
                            @ApiResponse(code = 400, message = "Validation errors occurred") })
    @PostMapping(value = "/update/{example}",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = { MediaType.TEXT_PLAIN_VALUE,
                              MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity updateExample(
            @ApiParam(value = "Update a specific example using form validation", required = true)
            @PathVariable String example,
            @RequestHeader(HttpHeaders.ACCEPT) String accept,
            @RequestBody(required = false) @Validated ExampleForm exampleForm,
            BindingResult bindingResult)
    {
        if (example.equals("not-found"))
        {
            return getNotFoundResponseEntity(NOT_FOUND_MESSAGE, accept);
        }

        // In case of form validation failures - throw a RequestBodyValidationException.
        // This will be automatically handled afterwards.
        if (exampleForm == null)
        {
            throw new RequestBodyValidationException("Empty request body", bindingResult);
        }
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException("Validation error", bindingResult);
        }

        return getSuccessfulResponseEntity("Credentials have been successfully updated.", accept);
    }

    @ApiOperation(value = "Delete an example")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Everything went ok"),
                            @ApiResponse(code = 404, message = "Example could not be found.") })
    @DeleteMapping(value = "/delete/{example}",
                   consumes = MediaType.APPLICATION_JSON_VALUE,
                   produces = { MediaType.TEXT_PLAIN_VALUE,
                                MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity deleteExample(@ApiParam(value = "Delete a specific example", required = true)
                                        @PathVariable String example,
                                        @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        if (example.equals("not-found"))
        {
            return getNotFoundResponseEntity(NOT_FOUND_MESSAGE, accept);
        }

        return getSuccessfulResponseEntity("example has been successfully deleted.", accept);
    }

    @ApiOperation(value = "Handling exceptions")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Everything went ok"),
                            @ApiResponse(code = 500, message = "Something really bad and unpredictable happened.") })
    @GetMapping(value = "/handle-exception",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity handleExceptions(@RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        try
        {
            throw new Exception("Something bad happened.");
        }
        catch (Exception e)
        {
            String message = "This example message will be logged in the logs and sent to the client.";
            return getExceptionResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, message, e, accept);
        }
    }
}
