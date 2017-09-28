package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.event.cron.CronTaskEvent;
import org.carlspring.strongbox.event.cron.CronTaskEventListener;
import org.carlspring.strongbox.event.cron.CronTaskEventListenerRegistry;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertNotNull;

/**
 * @author carlspring
 */
public class BaseCronTestCase
        implements CronTaskEventListener
{

    private final Logger logger = LoggerFactory.getLogger(BaseCronTestCase.class);

    public static final long CRON_TASK_MAX_WAIT = 15000L;

    public static final long CRON_TASK_CHECK_INTERVAL = 500L;

    @Inject
    protected CronTaskEventListenerRegistry cronTaskEventListenerRegistry;

    @Inject
    protected CronJobSchedulerService cronJobSchedulerService;

    @Inject
    protected CronTaskConfigurationService cronTaskConfigurationService;

    protected String expectedCronTaskName;

    protected int expectedEventType;

    protected CronTaskEvent receivedEvent;

    protected boolean receivedExpectedEvent = false;


    public CronTaskConfiguration addCronJobConfig(CronTaskConfiguration cronTaskConfiguration)
            throws Exception
    {
        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);

        CronTaskConfiguration configuration = cronTaskConfigurationService.findOne(cronTaskConfiguration.getName());

        assertNotNull("Failed to save cron configuration!", configuration);

        return configuration;
    }

    @Override
    public void handle(CronTaskEvent event)
    {
        logger.debug("Received event type " + event.getType() + " for " + event.getName());

        if (event.getType() == expectedEventType &&
            (event.getName() != null && event.getName().equals(expectedCronTaskName)))
        {
            receivedExpectedEvent = true;
            receivedEvent = event;

            logger.debug("Received expected event: " + expectedEventType);
        }
    }

    public boolean expectEvent(String cronTaskName, int expectedEventType)
            throws InterruptedException
    {
        return expectEvent(cronTaskName, expectedEventType, 0, CRON_TASK_CHECK_INTERVAL);
    }

    /**
     * Waits for an event to occur.
     *
     * @param expectedEventType The event type to wait for
     * @param maxWaitTime       The maximum wait time (in milliseconds)
     * @param checkInterval     The interval (in milliseconds) at which to check for the occurrence of the event
     */
    public boolean expectEvent(String expectedCronTaskName,
                               int expectedEventType,
                               long maxWaitTime,
                               long checkInterval)
            throws InterruptedException
    {
        this.expectedCronTaskName = expectedCronTaskName;
        this.expectedEventType = expectedEventType;

        logger.debug("Expecting event type " + expectedEventType + " for '" + expectedCronTaskName + "'...");

        int totalWait = 0;
        while (!receivedExpectedEvent &&
               (maxWaitTime > 0 && totalWait <= maxWaitTime || // If a maxWaitTime has been defined,
                maxWaitTime == 0))                             // otherwise, default to forever
        {
            Thread.sleep(checkInterval);
            totalWait += checkInterval;
        }

        return receivedExpectedEvent;
    }

    public int getExpectedEventType()
    {
        return expectedEventType;
    }

    public void setExpectedEventType(int expectedEventType)
    {
        this.expectedEventType = expectedEventType;
    }

    public CronTaskEvent getReceivedEvent()
    {
        return receivedEvent;
    }

    public void setReceivedEvent(CronTaskEvent receivedEvent)
    {
        this.receivedEvent = receivedEvent;
    }

    public boolean isReceivedExpectedEvent()
    {
        return receivedExpectedEvent;
    }

    public void setReceivedExpectedEvent(boolean receivedExpectedEvent)
    {
        this.receivedExpectedEvent = receivedExpectedEvent;
    }

}
