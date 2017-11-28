package org.carlspring.strongbox.cron.jobs;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.CronTaskConfigurationService;
import org.carlspring.strongbox.cron.services.CronTaskDataService;
import org.carlspring.strongbox.cron.services.support.CronTaskConfigurationSearchCriteria;
import org.carlspring.strongbox.data.service.support.search.PagingCriteria;

import javax.inject.Inject;
import java.util.List;

import static org.carlspring.strongbox.cron.services.support.CronTaskConfigurationSearchCriteria.CronTaskConfigurationSearchCriteriaBuilder.aCronTaskConfigurationSearchCriteria;

/**
 * @author Przemyslaw Fusik
 */
public abstract class OnePerRepositoryJavaCronJob
        extends JavaCronJob
{

    @Inject
    private CronTaskDataService cronTaskDataService;

    @Inject
    private CronTaskConfigurationService cronTaskConfigurationService;

    @Override
    public void beforeScheduleCallback(final CronTaskConfiguration notYetScheduledConfiguration)
            throws Exception
    {
        final String storageId = notYetScheduledConfiguration.getProperty("storageId");
        final String repositoryId = notYetScheduledConfiguration.getProperty("repositoryId");

        final CronTaskConfigurationSearchCriteria searchCriteria = aCronTaskConfigurationSearchCriteria()
                                                                           .withProperty("storageId", storageId)
                                                                           .withProperty("repositoryId", repositoryId)
                                                                           .withProperty("jobClass",
                                                                                         getClass().getName())
                                                                           .build();

        final List<CronTaskConfiguration> previousConfigurations = cronTaskDataService.findMatching(searchCriteria,
                                                                                                    PagingCriteria.ALL);
        for (final CronTaskConfiguration storedConfiguration : previousConfigurations)
        {
            if (!storedConfiguration.equals(notYetScheduledConfiguration))
            {
                // remove previous configurations for the same job and repository
                cronTaskConfigurationService.deleteConfiguration(storedConfiguration);
            }

        }
    }

}
