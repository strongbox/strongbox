package org.carlspring.strongbox.security.jaas.authentication;

/**
 * @author mtodorov
 */
public class UserStorageException extends Exception
{

    public UserStorageException()
    {
    }

    public UserStorageException(String message)
    {
        super(message);
    }

    public UserStorageException(String message,
                                Throwable cause)
    {
        super(message, cause);
    }

    public UserStorageException(Throwable cause)
    {
        super(cause);
    }

}
