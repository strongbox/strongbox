package org.carlspring.strongbox.authentication.api.impl.jwt;

import org.carlspring.strongbox.authentication.api.AuthenticationSupplier;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

public class JWTAuthenticationSupplier
        implements AuthenticationSupplier
{

    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationSupplier.class);

    private static final Pattern BEARER_PATTERN = Pattern.compile("Bearer (.*)");

    @CheckForNull
    @Override
    public Authentication supply(@Nonnull HttpServletRequest request)
    {
        final String tokenHeader = request.getHeader("Authorization");
        if (tokenHeader == null)
        {
            return null;
        }

        final Matcher matcher = BEARER_PATTERN.matcher(tokenHeader);
        if (!matcher.matches())
        {
            return null;
        }

        final String token = matcher.group(1);

        logger.debug("Bearer Authorization header found with token '" + token + "'");
        return new JWTAuthentication(token);
    }
}
