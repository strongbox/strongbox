package org.carlspring.strongbox.security.authentication.suppliers;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.carlspring.strongbox.authentication.api.password.PasswordAuthentication;
import org.carlspring.strongbox.controllers.layout.npm.NpmUser;
import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.regex.Matcher;

@Component
public class NpmLoginAuthenticationSupplier
        extends LayoutAuthenticationSupplier
{

    @Inject
    private ObjectMapper objectMapper;

    public NpmLoginAuthenticationSupplier()
    {
        super(NpmLayoutProvider.ALIAS);
    }

    @CheckForNull
    @Override
    public Authentication supply(@Nonnull HttpServletRequest request)
    {
        NpmUser npmUser = deserializeNpmUser(request);

        if (!isValidNpmUser(npmUser) ||
            !usernamesMatch(request.getRequestURI(), npmUser.getName()))
        {
            throw new BadCredentialsException("invalid.credentials");
        }

        return new PasswordAuthentication(npmUser.getName(), npmUser.getPassword());
    }

    @Override
    public boolean supports(@Nonnull HttpServletRequest request)
    {
        if (!super.supports(request))
        {
            return false;
        }

        return RequestMethod.PUT.name().equalsIgnoreCase(request.getMethod()) &&
               request.getRequestURI().contains(NpmLayoutProvider.NPM_USER_PATH);
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
               npmUser.getPassword() != null;
    }

    private boolean usernamesMatch(String url, String bodyUsername)
    {
        Matcher urlUsernameMatcher = NpmLayoutProvider.NPM_URL_USERNAME_PATTERN.matcher(url);

        return  urlUsernameMatcher.find() &&
                urlUsernameMatcher.group(1).equals(bodyUsername);
    }

}
