package org.carlspring.strongbox.exception;

/**
 * @author Pablo Tirado
 */
public class StorageNotFoundException
        extends RuntimeException
{

    public StorageNotFoundException(final String message)
    {
        super(message);
    }

}
