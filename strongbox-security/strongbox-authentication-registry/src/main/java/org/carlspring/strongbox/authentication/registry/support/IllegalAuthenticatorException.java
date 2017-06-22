package org.carlspring.strongbox.authentication.registry.support;

/**
 * @author Przemyslaw Fusik
 */
public class IllegalAuthenticatorException extends RuntimeException
{

    public IllegalAuthenticatorException(String message)
    {
        super(message);
    }
}
