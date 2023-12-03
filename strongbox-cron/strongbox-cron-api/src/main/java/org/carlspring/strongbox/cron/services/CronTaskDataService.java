package org.carlspring.strongbox.cron.services;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.cron.services.support.CronTaskConfigurationSearchCriteria;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @author Yougeshwar
 * @author Pablo Tirado
 */
public interface CronTaskDataService
{

    CronTaskConfigurationDto getTaskConfigurationDto(UUID cronTaskConfigurationUuid);

    CronTasksConfigurationDto getTasksConfigurationDto();

    List<CronTaskConfiguration> findMatching(CronTaskConfigurationSearchCriteria searchCriteria);

    UUID save(CronTaskConfigurationDto configuration) throws IOException;

    void delete(UUID cronTaskConfigurationUuid) throws IOException;


}
