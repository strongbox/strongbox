package org.carlspring.strongbox.users.security;

/**
 * @author Przemyslaw Fusik
 */
public abstract class AuthorizationConfigException
        extends RuntimeException
{

    public AuthorizationConfigException(final Throwable cause)
    {
        super(cause);
    }

    public AuthorizationConfigException(final String message)
    {
        super(message);
    }
}
