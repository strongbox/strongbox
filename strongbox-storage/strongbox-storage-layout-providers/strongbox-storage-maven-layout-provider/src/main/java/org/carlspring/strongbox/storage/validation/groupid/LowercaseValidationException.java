package org.carlspring.strongbox.storage.validation.groupid;

/**
 * Created by dinesh on 12/6/17.
 */
public class LowercaseValidationException
        extends Exception
{

    public LowercaseValidationException()
    {

    }

    public LowercaseValidationException(String message)
    {
        super(message);
    }

    public LowercaseValidationException(String message,
                                        Throwable cause)
    {
        super(message, cause);
    }

    public LowercaseValidationException(Throwable cause)
    {
        super(cause);
    }

}

