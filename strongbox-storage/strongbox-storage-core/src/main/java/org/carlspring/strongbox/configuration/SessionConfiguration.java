package org.carlspring.strongbox.configuration;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class SessionConfiguration
{

    private final Integer timeoutSeconds;


    public SessionConfiguration(final MutableSessionConfiguration delegate)
    {
        this.timeoutSeconds = delegate.getTimeoutSeconds();
    }

    public Integer getTimeoutSeconds()
    {
        return timeoutSeconds;
    }
}
