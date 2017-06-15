package org.carlspring.strongbox.security.exceptions;

import org.springframework.security.core.AuthenticationException;

public class SecurityTokenException extends AuthenticationException
{

    public SecurityTokenException(String msg, Throwable t)
    {
        super(msg, t);
    }

    public SecurityTokenException(String msg)
    {
        super(msg);
    }

}
