package org.carlspring.strongbox.validation;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Pablo Tirado
 */
public class ServiceUnavailableException
        extends RuntimeException
{

    public ServiceUnavailableException()
    {
        this(StringUtils.EMPTY);
    }

    public ServiceUnavailableException(final String message)
    {
        super(message);
    }

    public ServiceUnavailableException(final String message,
                                       final Throwable cause)
    {
        super(message, cause);
    }

    public ServiceUnavailableException(final Throwable cause)
    {
        super(cause);
    }

    public ServiceUnavailableException(final String message,
                                       final Throwable cause,
                                       final boolean enableSuppression,
                                       final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
