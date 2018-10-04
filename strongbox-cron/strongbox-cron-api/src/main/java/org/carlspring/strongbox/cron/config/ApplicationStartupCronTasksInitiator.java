package org.carlspring.strongbox.cron.config;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class ApplicationStartupCronTasksInitiator
{

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupCronTasksInitiator.class);

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Inject
    private CronTasksConfigurationFileManager cronTasksConfigurationFileManager;

    @PostConstruct
    public void postConstruct()
            throws Exception
    {
        CronTasksConfigurationDto cronTasksConfiguration = cronTasksConfigurationFileManager.read();
        for (Iterator<CronTaskConfigurationDto> iterator = cronTasksConfiguration.getCronTaskConfigurations().iterator(); iterator.hasNext();)
        {
            CronTaskConfigurationDto configuration = iterator.next();

            logger.debug("Saving cron configuration {}", configuration);

            String jobClass = configuration.getProperty("jobClass");
            if (jobClass != null && !jobClass.trim().isEmpty())
            {
                try
                {
                    Class.forName(jobClass);
                }
                catch (ClassNotFoundException e)
                {
                    logger.warn(String.format("Skip configuration, job class not found [%s].", jobClass));
                    iterator.remove();
                    continue;
                }
            }

        }
        cronTaskConfigurationService.setConfiguration(cronTasksConfiguration);
    }


}
