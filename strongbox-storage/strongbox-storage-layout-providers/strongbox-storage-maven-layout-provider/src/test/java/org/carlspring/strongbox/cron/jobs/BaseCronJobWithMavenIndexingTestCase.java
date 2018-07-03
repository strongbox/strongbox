package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.event.cron.CronTaskEvent;
import org.carlspring.strongbox.event.cron.CronTaskEventListener;
import org.carlspring.strongbox.event.cron.CronTaskEventListenerRegistry;
import org.carlspring.strongbox.event.cron.CronTaskEventTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertNotNull;

/**
 * @author carlspring
 */
public class BaseCronJobWithMavenIndexingTestCase
        extends TestCaseWithMavenArtifactGenerationAndIndexing
        implements CronTaskEventListener
{

    public static final long CRON_TASK_CHECK_INTERVAL = 500L;

    @Inject
    protected CronTaskEventListenerRegistry cronTaskEventListenerRegistry;

    @Inject
    protected CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    protected JobManager jobManager;

    /**
     * A map containing the cron task configurations used by this test.
     */
    protected Map<String, CronTaskConfiguration> cronTaskConfigurations = new LinkedHashMap<>();

    protected int expectedEventType = CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType();

    protected CronTaskEvent receivedEvent;

    protected boolean receivedExpectedEvent;

    protected String expectedJobName;

    @Before
    public void listenerSetup()
            throws Exception
    {
        // Register to receive cron task-related events
        cronTaskEventListenerRegistry.addListener(this);
    }

    @After
    public void listenerTearDown()
            throws Exception
    {
        // Un-register to receive cron task-related events
        cronTaskEventListenerRegistry.removeListener(this);
    }

    protected CronTaskConfiguration addCronJobConfig(String jobName,
                                                     Class<? extends JavaCronJob> className,
                                                     String storageId,
                                                     String repositoryId)
            throws Exception
    {
        return addCronJobConfig(jobName, className, storageId, repositoryId, null);
    }

    protected CronTaskConfiguration addCronJobConfig(String jobName,
                                                     Class<? extends JavaCronJob> className,
                                                     String storageId,
                                                     String repositoryId,
                                                     Consumer<Map<String, String>> additionalProperties)
            throws Exception
    {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("cronExpression", "0 11 11 11 11 ? 2100");
        properties.put("storageId", storageId);
        properties.put("repositoryId", repositoryId);
        if (additionalProperties != null)
        {
            additionalProperties.accept(properties);
        }
        return addCronJobConfig(jobName, className, properties);
    }

    protected CronTaskConfiguration addCronJobConfig(String jobName,
                                                     Class<? extends JavaCronJob> className,
                                                     Map<String, String> properties)
            throws Exception
    {
        CronTaskConfiguration cronTaskConfiguration = new CronTaskConfiguration();
        cronTaskConfiguration.setOneTimeExecution(true);
        cronTaskConfiguration.setImmediateExecution(true);
        cronTaskConfiguration.setName(jobName);
        cronTaskConfiguration.addProperty("jobClass", className.getName());

        for (String propertyKey : properties.keySet())
        {
            cronTaskConfiguration.addProperty(propertyKey, properties.get(propertyKey));
        }

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);

        CronTaskConfiguration configuration = cronTaskConfigurationService.findOne(jobName);

        assertNotNull("Failed to save cron configuration!", configuration);

        addCronTaskConfiguration(jobName, configuration);

        return cronTaskConfiguration;
    }

    @Override
    public void handle(CronTaskEvent event)
    {
        if (event.getType() == expectedEventType && expectedJobName.equals(event.getName()))
        {
            receivedExpectedEvent = true;
            receivedEvent = event;
        }
    }

    public boolean expectEvent()
            throws InterruptedException
    {
        return expectEvent(10000, CRON_TASK_CHECK_INTERVAL);
    }

    /**
     * Waits for an event to occur.
     *
     * @param maxWaitTime   The maximum wait time (in milliseconds)
     * @param checkInterval The interval (in milliseconds) at which to check for the occurrence of the event
     */
    public boolean expectEvent(long maxWaitTime,
                               long checkInterval)
            throws InterruptedException
    {
        int totalWait = 0;
        while (!receivedExpectedEvent &&
               (maxWaitTime > 0 && totalWait <= maxWaitTime || // If a maxWaitTime has been defined,
                maxWaitTime == 0))                            // otherwise, default to forever
        {
            Thread.sleep(checkInterval);
            totalWait += checkInterval;
        }

        return receivedExpectedEvent;
    }

    public CronTaskConfiguration addCronTaskConfiguration(String key,
                                                          CronTaskConfiguration value)
    {
        return cronTaskConfigurations.put(key, value);
    }

}
