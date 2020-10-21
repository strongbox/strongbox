package org.carlspring.strongbox.exception;

/**
 * @author Pablo Tirado
 */
public class RepositoryNotFoundException
        extends RuntimeException
{

    public RepositoryNotFoundException(final String message)
    {
        super(message);
    }

}
