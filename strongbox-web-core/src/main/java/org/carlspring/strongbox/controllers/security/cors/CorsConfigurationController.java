package org.carlspring.strongbox.controllers.security.cors;

import org.carlspring.strongbox.controllers.BaseController;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * @author Przemyslaw Fusik
 */
@RestController
@PreAuthorize("hasAuthority('ADMIN')")
@RequestMapping(value = "/configuration/cors")
@Api(value = "/configuration/cors")
public class CorsConfigurationController
        extends BaseController
{

    @Inject
    private CorsConfigurationSource corsConfigurationSource;

    @ApiOperation(value = "Returns allowed origins", httpMethod = "GET", produces = MediaType.APPLICATION_XML_VALUE +
                                                                                    "," +
                                                                                    MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = @ApiResponse(code = 200, message = "Allowed origins."))
    @RequestMapping(value = "/", method = RequestMethod.GET, produces = { MediaType.APPLICATION_XML_VALUE,
                                                                          MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getAllowedOrigins()
    {
        return ResponseEntity.ok(
                ((UrlBasedCorsConfigurationSource) corsConfigurationSource).getCorsConfigurations()
                                                                           .values()
                                                                           .stream()
                                                                           .flatMap(c -> c.getAllowedOrigins()
                                                                                          .stream())
                                                                           .collect(Collectors.toList()));
    }

    @ApiOperation(value = "Sets CORS allowed origins", httpMethod = "PUT", consumes = MediaType.APPLICATION_JSON_VALUE,
            notes = "In the request body, put an array of all allowed origins like this example: " +
                    "[{\"http://dev.carlspring.org/confluence\"},{\"http://dev.carlspring.org/jenkins\"}]")
    @ApiResponses(value = @ApiResponse(code = 200, message = "CORS allowed origins set succeeded"))
    @RequestMapping(value = "/", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity setAllowedOrigins(@RequestBody List<String> allowedOrigins)
    {
        ((UrlBasedCorsConfigurationSource) corsConfigurationSource).getCorsConfigurations()
                                                                   .values()
                                                                   .stream()
                                                                   .findFirst()
                                                                   .get()
                                                                   .setAllowedOrigins(allowedOrigins);

        return ResponseEntity.ok("CORS allowed origins set succeeded");
    }
}
