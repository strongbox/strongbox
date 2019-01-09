package org.carlspring.strongbox.cron.services;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.GroovyScriptNamesDto;

/**
 * @author carlspring
 */
public interface CronJobSchedulerService
{

    void scheduleJob(CronTaskConfigurationDto cronTaskConfiguration);

    void deleteJob(String cronTaskConfigurationUuid);

    GroovyScriptNamesDto getGroovyScriptsName();
}
