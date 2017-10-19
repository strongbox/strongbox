package org.carlspring.strongbox.event.cron;

import org.carlspring.strongbox.event.AbstractEventListenerRegistry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author carlspring
 */
@Component
public class CronTaskEventListenerRegistry
        extends AbstractEventListenerRegistry
{

    private static final Logger logger = LoggerFactory.getLogger(CronTaskEventListenerRegistry.class);


    public void dispatchCronTaskCreatedEvent(String name)
    {
        CronTaskEvent event = new CronTaskEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_SAVED.getType(), name);

        logger.debug("Dispatching CronTaskEventTypeEnum.EVENT_CRON_TASK_SAVED event for '" + name + "'...");

        dispatchEvent(event);
    }

    public void dispatchCronTaskDeletedEvent(String name)
    {
        CronTaskEvent event = new CronTaskEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_DELETED.getType(), name);

        logger.debug("Dispatching CronTaskEventTypeEnum.EVENT_CRON_TASK_DELETED event for '" + name + "'...");

        dispatchEvent(event);
    }

    public void dispatchCronTaskExecutingEvent(String name)
    {
        CronTaskEvent event = new CronTaskEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTING.getType(), name);

        logger.debug("Dispatching CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTING event for '" + name + "'...");

        dispatchEvent(event);
    }

    public void dispatchCronTaskExecutedEvent(String name)
    {
        CronTaskEvent event = new CronTaskEvent(CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType(), name);

        logger.debug("Dispatching CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE event for '" + name + "'...");

        dispatchEvent(event);
    }

}
