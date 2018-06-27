package org.carlspring.strongbox.cron.exceptions;

/**
 * @author Przemyslaw Fusik
 */
public class CronTaskConfigurationException
        extends RuntimeException
{

    public CronTaskConfigurationException(final String message)
    {
        super(message);
    }
}
