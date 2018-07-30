package org.carlspring.strongbox.security.authentication.suppliers;

import java.util.Optional;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.carlspring.strongbox.authentication.api.impl.xml.JwtAuthentication;
import org.carlspring.strongbox.security.authentication.JwtTokenFetcher;
import org.carlspring.strongbox.users.security.SecurityTokenProvider;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Order(2)
class JWTAuthenticationSupplier implements AuthenticationSupplier, JwtTokenFetcher
{

    @Inject
    private SecurityTokenProvider securityTokenProvider;

    @CheckForNull
    @Override
    public Authentication supply(@Nonnull HttpServletRequest request)
    {
        final Optional<String> optToken = getToken(request);
        if (!optToken.isPresent())
        {
            return null;
        }

        final String token = optToken.get();
        String username = securityTokenProvider.getSubject(token);

        return new JwtAuthentication(username, token);
    }

    @Override
    public boolean supports(HttpServletRequest request)
    {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader != null && authHeader.startsWith(BEARER_AUTHORIZATION_PREFIX))
        {
            return true;
        }

        return false;
    }

}
