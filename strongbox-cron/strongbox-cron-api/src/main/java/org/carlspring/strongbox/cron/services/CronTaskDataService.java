package org.carlspring.strongbox.cron.services;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.services.support.CronTaskConfigurationSearchCriteria;

import java.util.List;

/**
 * @author Yougeshwar
 */
public interface CronTaskDataService
{

    CronTaskConfigurationDto getTaskConfigurationDto(String cronTaskConfigurationUuid);

    CronTasksConfigurationDto getTasksConfigurationDto();

    List<CronTaskConfiguration> findMatching(CronTaskConfigurationSearchCriteria searchCriteria);

    void save(CronTaskConfigurationDto configuration);

    void delete(String uuid);


}
