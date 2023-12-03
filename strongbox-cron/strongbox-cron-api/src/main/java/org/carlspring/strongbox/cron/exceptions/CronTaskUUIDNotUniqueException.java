package org.carlspring.strongbox.cron.exceptions;

/**
 * @author Pablo Tirado
 */
public class CronTaskUUIDNotUniqueException
        extends RuntimeException
{

    public CronTaskUUIDNotUniqueException(final String message)
    {
        super(message);
    }
}