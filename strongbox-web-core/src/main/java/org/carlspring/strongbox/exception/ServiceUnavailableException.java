package org.carlspring.strongbox.exception;

/**
 * @author Pablo Tirado
 */
public class ServiceUnavailableException
        extends RuntimeException
{

    public ServiceUnavailableException(final String message)
    {
        super(message);
    }

}
