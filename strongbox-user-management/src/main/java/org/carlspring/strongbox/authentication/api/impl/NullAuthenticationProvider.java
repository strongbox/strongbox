package org.carlspring.strongbox.authentication.api.impl;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;

/**
 * @author Przemyslaw Fusik
 */
public class NullAuthenticationProvider
        implements AuthenticationProvider
{

    public static final NullAuthenticationProvider INSTANCE = new NullAuthenticationProvider();

    @Override
    public Authentication authenticate(Authentication authentication)
    {
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return false;
    }
}
