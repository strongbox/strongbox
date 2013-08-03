package org.carlspring.strongbox.annotations;

/**
 * @author mtodorov
 */
public class InjectionException extends Exception
{

    public InjectionException()
    {
        super();
    }

    public InjectionException(String message)
    {
        super(message);
    }

    public InjectionException(String message,
                              Throwable cause)
    {
        super(message, cause);
    }

    public InjectionException(Throwable cause)
    {
        super(cause);
    }

    public InjectionException(String message,
                              Throwable cause,
                              boolean enableSuppression,
                              boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
