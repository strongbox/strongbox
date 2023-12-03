package org.carlspring.strongbox.security.authentication.suppliers;

import java.util.Enumeration;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.carlspring.strongbox.authentication.api.nuget.SecurityTokenAuthentication;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.security.exceptions.InvalidTokenException;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.stereotype.Component;

/**
 * @author Sergey Bespalov
 */
@Component
public class NugetApiKeyAuthenticationSupplier
        extends LayoutAuthenticationSupplier
{

    public static final String HEADER_NUGET_APIKEY = "x-nuget-apikey";

    @Inject
    private SecurityTokenProvider securityTokenProvider;

    public NugetApiKeyAuthenticationSupplier()
    {
        super(NugetLayoutProvider.ALIAS);
    }

    @Override
    public Authentication supply(@Nonnull HttpServletRequest request)
    {
        final String nugetApiKey = request.getHeader(HEADER_NUGET_APIKEY);
        if (nugetApiKey == null)
        {
            throw new PreAuthenticatedCredentialsNotFoundException("Unauthorized");
        }

        String username;
        try
        {
            username = securityTokenProvider.getSubject(nugetApiKey);
        }
        catch (InvalidTokenException e)
        {
            throw new BadCredentialsException("Invalid token");
        }

        return new SecurityTokenAuthentication(username, nugetApiKey);
    }

    @Override
    public boolean supports(@Nonnull HttpServletRequest request)
    {
        if (!super.supports(request))
        {
            return false;
        }

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements())
        {
            String headerName = (String) headerNames.nextElement();
            if (!HEADER_NUGET_APIKEY.equalsIgnoreCase(headerName))
            {
                continue;
            }
            return true;
        }

        return false;
    }
}
