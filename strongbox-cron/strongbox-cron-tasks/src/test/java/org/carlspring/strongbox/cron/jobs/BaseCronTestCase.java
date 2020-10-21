package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.event.cron.CronTaskEvent;
import org.carlspring.strongbox.event.cron.CronTaskEventTypeEnum;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author carlspring
 */
abstract class BaseCronTestCase implements ApplicationListener<CronTaskEvent>, ApplicationContextAware
{

    public static final long CRON_TASK_CHECK_INTERVAL = 500L;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    protected CronTaskConfigurationService cronTaskConfigurationService;

    protected UUID expectedCronTaskUuid;

    protected String expectedCronTaskName;

    protected int expectedEventType = CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType();

    protected CronTaskEvent receivedEvent;

    protected boolean receivedExpectedEvent;

    public void init(TestInfo testInfo)
            throws Exception
    {
        expectedCronTaskUuid = UUID.randomUUID();

        Optional<Method> method = testInfo.getTestMethod();
        expectedCronTaskName = method.map(Method::getName).orElse(null);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException
    {
        ((ConfigurableApplicationContext) applicationContext).addApplicationListener(this);
    }

    public CronTaskConfigurationDto addCronJobConfig(CronTaskConfigurationDto cronTaskConfiguration)
            throws Exception
    {
        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);

        return cronTaskConfiguration;
    }

    @Override
    public void onApplicationEvent(CronTaskEvent event)
    {
        handle(event);
    }

    public void handle(CronTaskEvent event)
    {
        logger.debug("Received event type {} for {}. Expected event type {}, Expected event name {}", event.getType(),
                     event.getName(), expectedEventType, expectedCronTaskName);

        if (event.getType() == expectedEventType &&
            StringUtils.equals(expectedCronTaskUuid.toString(), event.getName()))
        {
            receivedExpectedEvent = true;
            receivedEvent = event;

            logger.debug("Received expected event: {}", expectedEventType);
        }
    }

    public boolean expectEvent()
            throws InterruptedException
    {
        return expectEvent(0, CRON_TASK_CHECK_INTERVAL);
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
        logger.debug("Expecting event type {} for '{}'...", expectedEventType, expectedCronTaskName);

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

}
