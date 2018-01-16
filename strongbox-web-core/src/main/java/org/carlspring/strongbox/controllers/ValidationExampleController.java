package org.carlspring.strongbox.controllers;

import org.carlspring.strongbox.controllers.support.ResponseEntityBody;
import org.carlspring.strongbox.forms.ExampleForm;
import org.carlspring.strongbox.validation.RequestBodyValidationException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Przemyslaw Fusik
 */
@RestController
@RequestMapping("/validation-example")
@Api(value = "/validation-example")
public class ValidationExampleController
{

    @ApiOperation(value = "Validation example operation.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Everything went ok"),
                            @ApiResponse(code = 400, message = "Validation errors occurred") })
    @PostMapping(value = "/post", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity post(@RequestBody(required = false) @Validated ExampleForm exampleForm,
                               BindingResult bindingResult)
    {
        if (exampleForm == null)
        {
            throw new RequestBodyValidationException("Empty request body", bindingResult);
        }
        if (bindingResult.hasErrors())
        {
            throw new RequestBodyValidationException("Validation error", bindingResult);
        }

        return ResponseEntity.ok(new ResponseEntityBody("successful operation"));
    }

}
