package org.carlspring.strongbox.authentication.api.impl.ldap;

import org.carlspring.strongbox.authentication.api.Authenticator;
import org.carlspring.strongbox.authentication.api.impl.NullAuthenticationProvider;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Optional;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class LdapAuthenticator
        implements Authenticator
{

    @Inject
    Optional<LdapAuthenticationProvider> authenticationProvider;

    @Nonnull
    @Override
    public AuthenticationProvider getAuthenticationProvider()
    {
        return authenticationProvider.isPresent() ? authenticationProvider.get() : NullAuthenticationProvider.INSTANCE;
    }
}
