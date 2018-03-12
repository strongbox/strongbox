package org.carlspring.strongbox.users.security;

/**
 * @author Przemyslaw Fusik
 */
public class AuthorizationConfigSaveException
        extends AuthorizationConfigException
{

    public AuthorizationConfigSaveException(final Throwable cause)
    {
        super(cause);
    }

    public AuthorizationConfigSaveException(final String message)
    {
        super(message);
    }
}
