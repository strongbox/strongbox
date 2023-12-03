package org.carlspring.strongbox.cron.services;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.cron.domain.GroovyScriptNamesDto;

import java.util.UUID;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
public interface CronJobSchedulerService
{

    void scheduleJob(CronTaskConfigurationDto cronTaskConfiguration);

    void deleteJob(UUID cronTaskConfigurationUuid);

    GroovyScriptNamesDto getGroovyScriptsName();
}
