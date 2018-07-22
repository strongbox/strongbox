package org.carlspring.strongbox.security.authentication.suppliers;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.carlspring.strongbox.authentication.api.impl.xml.SecurityTokenAuthentication;
import org.carlspring.strongbox.configuration.ConfigurationManager;
import org.carlspring.strongbox.providers.layout.NugetLayoutProvider;
import org.carlspring.strongbox.storage.Storage;
import org.carlspring.strongbox.storage.repository.Repository;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * @author Sergey Bespalov
 *
 */
@Component
public class NugetApiKeyAuthenticationSupplier implements AuthenticationSupplier
{

    @Inject
    private SecurityTokenProvider securityTokenProvider;

    @Inject
    private ConfigurationManager configurationManager;

    @Override
    public Authentication supply(@Nonnull HttpServletRequest request)
    {
        final String nugetApiKey = request.getHeader("x-nuget-apikey");
        if (nugetApiKey == null)
        {
            return null;
        }

        String username = securityTokenProvider.getSubject(nugetApiKey);
        String securityToken = (String) securityTokenProvider.getClaims(nugetApiKey)
                                                             .getClaimValue("security-token-key");

        return new SecurityTokenAuthentication(username, securityToken);
    }

    @Override
    public boolean supports(@Nonnull HttpServletRequest request)
    {
        String servletPath = request.getServletPath();
        if (!servletPath.startsWith("/storages"))
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

        return NugetLayoutProvider.ALIAS.equals(repository.getLayout());
    }

}
