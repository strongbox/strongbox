package org.carlspring.strongbox.security.authentication.suppliers;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.carlspring.strongbox.authentication.api.password.PasswordAuthentication;
import org.carlspring.strongbox.controllers.layout.npm.NpmArtifactController;
import org.carlspring.strongbox.controllers.layout.npm.NpmUser;

import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
@Order(4)
public class NpmLoginAuthenticationSupplier
        implements AuthenticationSupplier
{

    @Inject
    private ObjectMapper objectMapper;

    @CheckForNull
    @Override
    public Authentication supply(@Nonnull HttpServletRequest request)
    {
        NpmUser npmUser = deserializeNpmUser(request);

        if (isValidNpmUser(npmUser))
        {
            return new PasswordAuthentication(npmUser.getName(), npmUser.getPassword());
        }
        else
        {
            throw new BadCredentialsException("invalid.credentials");
        }

    }

    @Override
    public boolean supports(@Nonnull HttpServletRequest request)
    {
        return "PUT".equalsIgnoreCase(request.getMethod()) &&
                                      request.getContentType() != null &&
                                      request.getContentType().contains(MediaType.APPLICATION_JSON_VALUE) &&
                                      request.getRequestURI().contains("/-/user/org.couchdb.user:");
    }

    private NpmUser deserializeNpmUser(HttpServletRequest request)
    {
        NpmUser npmUser;

        try
        {
            npmUser = objectMapper.readValue(request.getInputStream(), NpmUser.class);
        }
        catch (IOException e)
        {
            npmUser = null;
        }

        return npmUser;
    }

    private boolean isValidNpmUser(NpmUser npmUser)
    {
        return npmUser != null &&
               npmUser.getName() != null &&
               npmUser.getPassword() != null &&
               npmUser.getDate() != null &&
               npmUser.getRoles() != null &&
               npmUser.getType() != null &&
               npmUser.getId() != null;
    }

}
