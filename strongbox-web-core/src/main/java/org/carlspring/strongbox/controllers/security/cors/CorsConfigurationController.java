package org.carlspring.strongbox.controllers.security.cors;

import org.carlspring.strongbox.controllers.BaseController;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
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

    @ApiOperation(value = "Returns allowed origins")
    @ApiResponses(value = @ApiResponse(code = 200, message = "Allowed origins."))
    @GetMapping(produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity getAllowedOrigins(@RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        List<String> allowedOrigins = ((UrlBasedCorsConfigurationSource) corsConfigurationSource).getCorsConfigurations()
                                                                                                 .values()
                                                                                                 .stream()
                                                                                                 .flatMap(
                                                                                                         c -> c.getAllowedOrigins() !=
                                                                                                              null ?
                                                                                                              c.getAllowedOrigins().stream() :
                                                                                                              Stream.empty())
                                                                                                 .collect(
                                                                                                         Collectors.toList());
        return ResponseEntity.ok(getListResponseEntityBody(allowedOrigins, accept));
    }

    @ApiOperation(value = "Sets CORS allowed origins",
            notes = "In the request body, put an array of all allowed origins like this example: " +
                    "[\"http://dev.carlspring.org/confluence\",\"http://dev.carlspring.org/jenkins\"]. " +
                    "You can always provide [*] to allow all origins.")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "CORS allowed origins was updated."),
                            @ApiResponse(code = 400, message = "Could not update CORS allowed origins.") })
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = { MediaType.TEXT_PLAIN_VALUE,
                             MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity setAllowedOrigins(@RequestBody List<String> allowedOrigins,
                                            @RequestHeader(HttpHeaders.ACCEPT) String accept)
    {
        Optional<CorsConfiguration> corsConfiguration = ((UrlBasedCorsConfigurationSource) corsConfigurationSource).getCorsConfigurations()
                                                                                                                   .values()
                                                                                                                   .stream()
                                                                                                                   .findFirst();

        if (corsConfiguration.isPresent())
        {
            corsConfiguration.get().setAllowedOrigins(allowedOrigins);
            return ResponseEntity.ok(getResponseEntityBody("CORS allowed origins was updated.", accept));
        }
        else
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(getResponseEntityBody("Could not update CORS allowed origins.", accept));
        }


    }
}
