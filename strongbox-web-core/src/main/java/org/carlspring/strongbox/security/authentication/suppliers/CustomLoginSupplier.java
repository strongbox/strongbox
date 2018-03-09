package org.carlspring.strongbox.security.authentication.suppliers;

import org.carlspring.strongbox.controllers.login.LoginController;
import org.carlspring.strongbox.controllers.login.LoginInput;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Works in conjunction {@link LoginController}
 *
 * @author Przemyslaw Fusik
 */
@Component
public class CustomLoginSupplier implements AuthenticationSupplier
{

    private static final Logger logger = LoggerFactory.getLogger(CustomLoginSupplier.class);

    @Inject
    private ObjectMapper objectMapper;

    @Override
    public Authentication supply(@Nonnull HttpServletRequest request)
    {
        LoginInput loginInput = null;
        try
        {
            loginInput = objectMapper.readValue(request.getInputStream(), LoginInput.class);
        }
        catch (IOException e)
        {
            logger.error(e.getMessage() ,e);
            return null;
        }

        return new UsernamePasswordAuthenticationToken(loginInput.getUsername(), loginInput.getPassword());
    }

    @Override
    public boolean supports(@Nonnull HttpServletRequest request)
    {
        return "POST".equalsIgnoreCase(request.getMethod()) &&
               MediaType.APPLICATION_JSON_VALUE.equals(request.getContentType()) &&
               LoginController.REQUEST_MAPPING.equals(request.getRequestURI());
    }
}
