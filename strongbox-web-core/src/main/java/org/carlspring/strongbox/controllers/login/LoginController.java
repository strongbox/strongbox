package org.carlspring.strongbox.controllers.login;

import org.carlspring.strongbox.controllers.BaseController;
import org.carlspring.strongbox.services.ConfigurationManagementService;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;

import javax.inject.Inject;

import java.util.Collections;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import static org.carlspring.strongbox.controllers.login.LoginController.REQUEST_MAPPING;

/**
 * Works in conjunction with {@link org.carlspring.strongbox.security.authentication.suppliers.JsonFormLoginSupplier}
 *
 * @author Przemyslaw Fusik
 */
@RestController
@RequestMapping(value = REQUEST_MAPPING)
@Api(value = REQUEST_MAPPING)
public class LoginController
        extends BaseController
{

    public static final String REQUEST_MAPPING = "/api/login";

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Inject
    private SecurityTokenProvider securityTokenProvider;

    @Inject
    private ConfigurationManagementService configurationManagementService;

    @ApiOperation(value = "Returns the JWT authentication token for provided username and password")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns generated JWT token"),
                            @ApiResponse(code = 401, message = "Invalid credentials"),
                            @ApiResponse(code = 500, message = "org.springframework.security.core.Authentication " +
                                                               "fetched by the strongbox security implementation is not supported") })
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity login(Authentication authentication) {
        return formLogin(authentication);
    }
    
    @ApiOperation(value = "Returns the JWT authentication token for provided username and password")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Returns generated JWT token"),
                            @ApiResponse(code = 401, message = "Invalid credentials"),
                            @ApiResponse(code = 500, message = "org.springframework.security.core.Authentication " +
                                                               "fetched by the strongbox security implementation is not supported") })
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity formLogin(Authentication authentication)
    {
        if (authentication == null || !authentication.isAuthenticated())
        {
            throw new InsufficientAuthenticationException("unauthorized");
        }
        if (!(authentication instanceof UsernamePasswordAuthenticationToken))
        {
            return toResponseEntityError("Unsupported authentication class " + authentication.getClass().getName());
        }

        String token;
        try
        {
            Object principal = authentication.getPrincipal();
            String subject;
            if (principal instanceof UserDetails)
            {
                subject = ((UserDetails) principal).getUsername();
            }
            else
            {
                subject = principal.toString();
            }
            
            Integer timeout = configurationManagementService.getConfiguration().getSessionConfiguration().getTimeoutSeconds();
            token = securityTokenProvider.getToken(subject, Collections.emptyMap(), timeout);
        }
        catch (JoseException e)
        {
            logger.error("Unable to create JWT token.", e);
            return toResponseEntityError("Unable to create JWT token.", HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok().body(new LoginOutput(token, authentication.getAuthorities()));
    }


}
