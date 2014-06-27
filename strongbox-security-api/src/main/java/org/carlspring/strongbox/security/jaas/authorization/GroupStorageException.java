package org.carlspring.strongbox.security.jaas.authorization;

/**
 * @author mtodorov
 */
public class GroupStorageException extends Exception
{

    public GroupStorageException()
    {
    }

    public GroupStorageException(String message)
    {
        super(message);
    }

    public GroupStorageException(String message,
                                 Throwable cause)
    {
        super(message, cause);
    }

    public GroupStorageException(Throwable cause)
    {
        super(cause);
    }

}
