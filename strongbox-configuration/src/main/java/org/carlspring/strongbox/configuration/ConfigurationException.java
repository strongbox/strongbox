package org.carlspring.strongbox.configuration;

/**
 * Corresponds to any issues related to wrong configuration settings.
 *
 * @author Alex Oreshkevich
 */
public class ConfigurationException
        extends RuntimeException
{

    /**
     * Constructs a new runtime exception with the specified detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public ConfigurationException(String message)
    {
        super(message);
    }
}
