package org.carlspring.strongbox.security.authentication.suppliers;

import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.carlspring.strongbox.authentication.api.impl.xml.SecurityTokenAuthentication;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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

    @Override
    public Authentication supply(HttpServletRequest request)
    {
        final String nugetApiKey = request.getHeader("x-nuget-apikey");
        if (nugetApiKey == null)
        {
            return null;
        }
        String userName = securityTokenProvider.getSubject(nugetApiKey);
        String securityToken = (String) securityTokenProvider.getClaims(nugetApiKey).getClaimValue("security-token-key");
        return new SecurityTokenAuthentication(userName, securityToken);
    }

}
