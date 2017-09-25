package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronJobSchedulerService;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.event.cron.CronTaskEvent;
import org.carlspring.strongbox.event.cron.CronTaskEventListener;
import org.carlspring.strongbox.event.cron.CronTaskEventListenerRegistry;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author carlspring
 */
public class BaseCronJobWithMavenIndexingTestCase
        extends TestCaseWithMavenArtifactGenerationAndIndexing
        implements CronTaskEventListener
{

    public static final long CRON_TASK_MAX_WAIT = 15000L;

    public static final long CRON_TASK_CHECK_INTERVAL = 500L;

    @Inject
    protected CronTaskEventListenerRegistry cronTaskEventListenerRegistry;

    @Inject
    protected CronJobSchedulerService cronJobSchedulerService;

    @Inject
    protected CronTaskConfigurationService cronTaskConfigurationService;

    /**
     * A map containing the cron task configurations used by this test.
     */
    protected Map<String, CronTaskConfiguration> cronTaskConfigurations = new LinkedHashMap<>();

    protected int expectedEventType;

    protected CronTaskEvent receivedEvent;

    protected boolean receivedExpectedEvent = false;


    public CronTaskConfiguration addCronJobConfig(String name,
                                                  String className,
                                                  Map<String, String> properties)
            throws Exception
    {
        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setOneTimeExecution(true);
        cronTaskConfiguration.setImmediateExecution(true);
        cronTaskConfiguration.setName(name);
        cronTaskConfiguration.addProperty("jobClass", className);

        for (String propertyKey : properties.keySet())
        {
            cronTaskConfiguration.addProperty(propertyKey, properties.get(propertyKey));
        }

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);

        CronTaskConfiguration configuration = cronTaskConfigurationService.findOne(name);

        assertNotNull("Failed to save cron configuration!", configuration);

        addCronTaskConfiguration(name, configuration);

        return cronTaskConfiguration;
    }

    @Override
    public void handle(CronTaskEvent event)
    {
        if (event.getType() == event.getType())
        {
            receivedExpectedEvent = true;
            receivedEvent = event;

            System.out.println("Received expected event: " + expectedEventType);
        }
    }

    public boolean expectEvent(int expectedEventType)
            throws InterruptedException
    {
        return expectEvent(expectedEventType, 0, CRON_TASK_CHECK_INTERVAL);
    }

    /**
     * Waits for an event to occur.
     *
     * @param expectedEventType     The event type to wait for
     * @param maxWaitTime           The maximum wait time (in milliseconds)
     * @param checkInterval         The interval (in milliseconds) at which to check for the occurrence of the event
     */
    public boolean expectEvent(int expectedEventType, long maxWaitTime, long checkInterval)
            throws InterruptedException
    {
        this.expectedEventType = expectedEventType;

        int totalWait = 0;
        while (!receivedExpectedEvent &&
               (maxWaitTime > 0 && totalWait <= maxWaitTime || // If a maxWaitTime has been defined,
                maxWaitTime == 0 ))                            // otherwise, default to forever
        {
            Thread.sleep(checkInterval);
            totalWait += checkInterval;
        }

        return receivedExpectedEvent;
    }

    public CronTaskConfiguration getCronTaskConfiguration(String key)
    {
        return cronTaskConfigurations.get(key);
    }

    public CronTaskConfiguration addCronTaskConfiguration(String key,
                                                          CronTaskConfiguration value)
    {
        return cronTaskConfigurations.put(key, value);
    }

    public CronTaskConfiguration removeCronTaskConfiguration(String key)
    {
        return cronTaskConfigurations.remove(key);
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
