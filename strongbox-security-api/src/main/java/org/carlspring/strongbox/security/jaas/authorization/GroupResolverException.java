package org.carlspring.strongbox.security.jaas.authorization;

/**
 * @author mtodorov
 */
public class GroupResolverException extends Exception
{

    public GroupResolverException()
    {
    }

    public GroupResolverException(String message)
    {
        super(message);
    }

    public GroupResolverException(String message,
                                  Throwable cause)
    {
        super(message, cause);
    }

    public GroupResolverException(Throwable cause)
    {
        super(cause);
    }

}
