package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.configuration.Configuration;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.JobManager;
import org.carlspring.strongbox.event.cron.CronTaskEvent;
import org.carlspring.strongbox.event.cron.CronTaskEventTypeEnum;
import org.carlspring.strongbox.providers.io.RepositoryPathResolver;
import org.carlspring.strongbox.services.ArtifactMetadataService;
import org.carlspring.strongbox.services.ConfigurationManagementService;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author carlspring
 */
public class BaseCronJobWithMavenIndexingTestCase
{

    protected static final String STORAGE0 = "storage0";

    protected static final long EVENT_TIMEOUT_SECONDS = 30L;

    @Inject
    protected CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    protected JobManager jobManager;

    @Inject
    protected ConfigurationManagementService configurationManagementService;

    @Inject
    protected RepositoryPathResolver repositoryPathResolver;

    @Inject
    protected ArtifactMetadataService artifactMetadataService;

    @Inject
    private ApplicationContext applicationContext;

    private CronJobApplicationListener cronJobApplicationListener;

    protected UUID expectedJobKey;

    protected String expectedJobName;

    /**
     * A map containing the cron task configurations used by this test.
     */
    protected Map<String, CronTaskConfigurationDto> cronTaskConfigurations = new LinkedHashMap<>();

    protected CronTaskConfigurationDto addCronJobConfig(UUID jobKey,
                                                        String jobName,
                                                        Class<? extends JavaCronJob> className,
                                                        String storageId,
                                                        String repositoryId)
            throws Exception
    {
        return addCronJobConfig(jobKey, jobName, className, storageId, repositoryId, null);
    }

    protected CronTaskConfigurationDto addCronJobConfig(UUID jobKey,
                                                        String jobName,
                                                        Class<? extends JavaCronJob> className,
                                                        String storageId,
                                                        String repositoryId,
                                                        Consumer<Map<String, String>> additionalProperties)
            throws Exception
    {
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("storageId", storageId);
        properties.put("repositoryId", repositoryId);
        if (additionalProperties != null)
        {
            additionalProperties.accept(properties);
        }
        return addCronJobConfig(jobKey, jobName, className , properties );
    }

    protected CronTaskConfigurationDto addCronJobConfig(UUID jobKey,
                                                        String jobName,
                                                        Class<? extends JavaCronJob> className,
                                                        Map<String, String> properties)
            throws Exception
    {
        CronTaskConfigurationDto cronTaskConfiguration = new CronTaskConfigurationDto();
        cronTaskConfiguration.setCronExpression("0 11 11 11 11 ? 2100");
        cronTaskConfiguration.setOneTimeExecution(true);
        cronTaskConfiguration.setImmediateExecution(true);
        cronTaskConfiguration.setUuid(jobKey);
        cronTaskConfiguration.setName(jobName);
        cronTaskConfiguration.setJobClass(className.getName());

        for (String propertyKey : properties.keySet())
        {
            cronTaskConfiguration.addProperty(propertyKey, properties.get(propertyKey));
        }

        cronTaskConfigurationService.saveConfiguration(cronTaskConfiguration);

        addCronTaskConfiguration(jobName, cronTaskConfiguration);

        return cronTaskConfiguration;
    }

    protected CronTaskConfigurationDto addCronTaskConfiguration(String key,
                                                                CronTaskConfigurationDto value)
    {
        return cronTaskConfigurations.put(key, value);
    }

    protected Configuration getConfiguration()
    {
        return configurationManagementService.getConfiguration();
    }

    @BeforeEach
    public void init(final TestInfo testInfo)
            throws Exception
    {
        expectedJobKey = UUID.randomUUID();

        Optional<Method> method = testInfo.getTestMethod();
        expectedJobName = method.map(Method::getName).orElseThrow(() -> new IllegalStateException("No method name ?"));

        cronJobApplicationListener = new CronJobApplicationListener(expectedJobKey);

        ((ConfigurableApplicationContext) applicationContext).addApplicationListener(cronJobApplicationListener);
    }

    private static class CronJobApplicationListener
            implements ApplicationListener<CronTaskEvent>
    {

        private int expectedEventType = CronTaskEventTypeEnum.EVENT_CRON_TASK_EXECUTION_COMPLETE.getType();

        private AtomicBoolean receivedExpectedEvent = new AtomicBoolean(false);

        private final UUID expectedJobKey;

        public CronJobApplicationListener(final UUID expectedJobKey)
        {
            this.expectedJobKey = expectedJobKey;
        }

        @Override
        public void onApplicationEvent(CronTaskEvent event)
        {
            handle(event);
        }

        public void handle(CronTaskEvent event)
        {
            if (event.getType() == expectedEventType && StringUtils.equals(expectedJobKey.toString(), event.getName()))
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
