package org.carlspring.strongbox.validation;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Pablo Tirado
 */
public class ElementNotFoundException
        extends RuntimeException
{

    public ElementNotFoundException()
    {
        this(StringUtils.EMPTY);
    }

    public ElementNotFoundException(final String message)
    {
        super(message);
    }

    public ElementNotFoundException(final String message,
                                    final Throwable cause)
    {
        super(message, cause);
    }

    public ElementNotFoundException(final Throwable cause)
    {
        super(cause);
    }

    public ElementNotFoundException(final String message,
                                    final Throwable cause,
                                    final boolean enableSuppression,
                                    final boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
