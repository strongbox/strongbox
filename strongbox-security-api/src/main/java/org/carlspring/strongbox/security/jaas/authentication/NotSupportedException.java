package org.carlspring.strongbox.security.jaas.authentication;

/**
 * @author stodorov
 */
public class NotSupportedException extends Exception
{

    public NotSupportedException()
    {
    }

    public NotSupportedException(String message)
    {
        super(message);
    }

    public NotSupportedException(String message,
                                   Throwable cause)
    {
        super(message, cause);
    }

    public NotSupportedException(Throwable cause)
    {
        super(cause);
    }

}
