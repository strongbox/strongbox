package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.event.cron.CronTaskEvent;
import org.carlspring.strongbox.event.cron.CronTaskEventListenerRegistry;
import org.carlspring.strongbox.event.cron.CronTaskEventTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.Assert.assertNotNull;

/**
 * @author carlspring
 */
public class BaseCronJobWithMavenIndexingTestCase
        extends TestCaseWithMavenArtifactGenerationAndIndexing implements ApplicationContextAware, ApplicationListener<CronTaskEvent>
{

    protected static final long EVENT_TIMEOUT_SECONDS = 10L;

    @Inject
    protected CronTaskEventListenerRegistry cronTaskEventListenerRegistry;

    @Inject
    protected CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    protected JobManager jobManager;

    /**
     * A map containing the cron task configurations used by this test.
     */
    protected Map<String, CronTaskConfigurationDto> cronTaskConfigurations = new LinkedHashMap<>();

    protected int expectedEventType = CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType();

    protected CronTaskEvent receivedEvent;

    protected AtomicBoolean receivedExpectedEvent;

    protected String expectedJobName;
    
    

    @Override
    public void onApplicationEvent(CronTaskEvent event)
    {
        handle(event);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException
    {
        ((ConfigurableApplicationContext)applicationContext).addApplicationListener(this);
    }

    protected CronTaskConfigurationDto addCronJobConfig(String jobName,
                                                        Class<? extends JavaCronJob> className,
                                                        String storageId,
                                                        String repositoryId)
            throws Exception
    {
        return addCronJobConfig(jobName, className, storageId, repositoryId, null);
    }

    protected CronTaskConfigurationDto addCronJobConfig(String jobName,
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

    protected CronTaskConfigurationDto addCronJobConfig(String jobName,
                                                        Class<? extends JavaCronJob> className,
                                                        Map<String, String> properties)
            throws Exception
    {
        CronTaskConfigurationDto cronTaskConfiguration = new CronTaskConfigurationDto();
        cronTaskConfiguration.setOneTimeExecution(true);
        cronTaskConfiguration.setImmediateExecution(true);
        cronTaskConfiguration.setName(jobName);
        cronTaskConfiguration.addProperty("jobClass", className.getName());

        for (String propertyKey : properties.keySet())
        {
            cronTaskConfiguration.addProperty(propertyKey, properties.get(propertyKey));
        }

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);

        CronTaskConfigurationDto configuration = cronTaskConfigurationService.getTaskConfigurationDto(jobName);

        assertNotNull("Failed to save cron configuration!", configuration);

        addCronTaskConfiguration(jobName, configuration);

        return cronTaskConfiguration;
    }

    public void handle(CronTaskEvent event)
    {
        if (event.getType() == expectedEventType && expectedJobName.equals(event.getName()))
        {
            receivedExpectedEvent.set(true);
            receivedEvent = event;
        }
    }

    public CronTaskConfigurationDto addCronTaskConfiguration(String key,
                                                             CronTaskConfigurationDto value)
    {
        return cronTaskConfigurations.put(key, value);
    }

}
