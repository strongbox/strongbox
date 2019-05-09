package org.carlspring.strongbox.security.authentication.suppliers;

import java.util.Enumeration;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.carlspring.strongbox.authentication.api.impl.xml.SecurityTokenAuthentication;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.security.exceptions.InvalidTokenException;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;

import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import static org.carlspring.strongbox.web.Constants.ARTIFACT_ROOT_PATH;

/**
 * @author Sergey Bespalov
 *
 */
@Component
@Order(3)
public class NugetApiKeyAuthenticationSupplier implements AuthenticationSupplier
{

    public static final String HEADER_NUGET_APIKEY = "x-nuget-apikey";

    @Inject
    private SecurityTokenProvider securityTokenProvider;

    @Inject
    private ConfigurationManager configurationManager;

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
        String servletPath = request.getServletPath();
        if (!servletPath.startsWith(ARTIFACT_ROOT_PATH))
        {
            return false;
        }

        String[] pathParts = servletPath.split("/");
        if (pathParts.length < 4)
        {
            return false;
        }

        String storageId = pathParts[2];
        String repositoryId = pathParts[3];
        if (storageId == null || repositoryId == null)
        {
            return false;
        }

        Storage storage = configurationManager.getConfiguration().getStorage(storageId);
        if (storage == null)
        {
            return false;
        }

        Repository repository = storage.getRepository(repositoryId);
        if (repository == null)
        {
            return false;
        }

        if (!NugetLayoutProvider.ALIAS.equals(repository.getLayout()))
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
