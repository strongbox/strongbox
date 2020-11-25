package org.carlspring.strongbox.exception;

import org.carlspring.strongbox.configuration.ConfigurationException;

public class SizeFormatValidationException extends ConfigurationException
{

    public SizeFormatValidationException(final String message)
    {
        super(message);
    }

}
