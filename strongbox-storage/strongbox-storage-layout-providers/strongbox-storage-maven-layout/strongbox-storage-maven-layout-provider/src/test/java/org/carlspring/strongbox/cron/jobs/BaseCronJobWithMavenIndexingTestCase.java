package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.event.cron.CronTaskEvent;
import org.carlspring.strongbox.event.cron.CronTaskEventListenerRegistry;
import org.carlspring.strongbox.event.cron.CronTaskEventTypeEnum;
import org.carlspring.strongbox.testing.TestCaseWithMavenArtifactGenerationAndIndexing;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.junit.jupiter.api.TestInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author carlspring
 */
public class BaseCronJobWithMavenIndexingTestCase
        extends TestCaseWithMavenArtifactGenerationAndIndexing
{

    protected static final long EVENT_TIMEOUT_SECONDS = 10L;

    @Inject
    protected CronTaskEventListenerRegistry cronTaskEventListenerRegistry;

    @Inject
    protected CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    protected JobManager jobManager;

    @Inject
    private ApplicationContext applicationContext;

    private CronJobApplicationListener cronJobApplicationListener;

    protected String expectedJobName;

    /**
     * A map containing the cron task configurations used by this test.
     */
    protected Map<String, CronTaskConfigurationDto> cronTaskConfigurations = new LinkedHashMap<>();

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

        addCronTaskConfiguration(jobName, cronTaskConfiguration);

        return cronTaskConfiguration;
    }

    public CronTaskConfigurationDto addCronTaskConfiguration(String key,
                                                             CronTaskConfigurationDto value)
    {
        return cronTaskConfigurations.put(key, value);
    }

    public void init(final TestInfo testInfo)
            throws Exception
    {
        Optional<Method> method = testInfo.getTestMethod();
        expectedJobName = method.map(Method::getName).orElseThrow(() -> new IllegalStateException("No method name ?"));

        cronJobApplicationListener = new CronJobApplicationListener(expectedJobName);

        ((ConfigurableApplicationContext) applicationContext).addApplicationListener(cronJobApplicationListener);
    }

    private static class CronJobApplicationListener
            implements ApplicationListener<CronTaskEvent>
    {

        private int expectedEventType = CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType();

        private AtomicBoolean receivedExpectedEvent = new AtomicBoolean(false);

        private final String expectedJobName;

        public CronJobApplicationListener(final String expectedJobName)
        {
            this.expectedJobName = expectedJobName;
        }

        @Override
        public void onApplicationEvent(CronTaskEvent event)
        {
            handle(event);
        }

        public void handle(CronTaskEvent event)
        {
            if (event.getType() == expectedEventType && expectedJobName.equals(event.getName()))
            {
                receivedExpectedEvent.set(true);
            }
        }
    }

    protected AtomicBoolean receivedExpectedEvent()
    {
        return cronJobApplicationListener.receivedExpectedEvent;
    }

}
