package org.carlspring.strongbox.nuget.file;


public class NugetFormatException
        extends Exception
{

    public NugetFormatException(String message,
                                Throwable cause,
                                boolean enableSuppression,
                                boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public NugetFormatException(Throwable cause)
    {
        super(cause);
    }

    public NugetFormatException(String message,
                                Throwable cause)
    {
        super(message, cause);
    }

    public NugetFormatException(String message)
    {
        super(message);
    }

    public NugetFormatException()
    {
    }
}
