package org.carlspring.strongbox.security.exceptions;

/**
 * @author Przemyslaw Fusik
 */
public class SecurityTokenExpiredException extends SecurityTokenException
{

    public SecurityTokenExpiredException(String msg,
                                         Throwable t)
    {
        super(msg, t);
    }

    public SecurityTokenExpiredException(String msg)
    {
        super(msg);
    }
}
