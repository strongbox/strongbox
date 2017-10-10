package org.carlspring.strongbox.controllers.users;

import javax.inject.Inject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Przemyslaw Fusik
 */
@Controller
@RequestMapping("/users/password-encoder")
@Api(value = "/users/password-encoder")
public class PasswordEncoderController
{

    @Inject
    private PasswordEncoder passwordEncoder;

    @ApiOperation(value = "Encodes provided raw password")
    @ApiResponses(value = @ApiResponse(code = 200, message = "Returns encoded raw password"))
    @RequestMapping(value = "/{rawPassword}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity encode(@PathVariable String rawPassword)
    {
        return ResponseEntity.ok(passwordEncoder.encode(rawPassword));
    }

}
