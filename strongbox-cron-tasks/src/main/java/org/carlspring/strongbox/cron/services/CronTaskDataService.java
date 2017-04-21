package org.carlspring.strongbox.cron.services;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.data.service.CrudService;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Yougeshwar
 */
@Transactional
public interface CronTaskDataService
        extends CrudService<CronTaskConfiguration, String>
{

    List<CronTaskConfiguration> findByName(final String name);
}
