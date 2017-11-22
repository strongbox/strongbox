package org.carlspring.strongbox.cron.services;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.cron.services.support.CronTaskConfigurationSearchCriteria;
import org.carlspring.strongbox.data.service.CrudService;
import org.carlspring.strongbox.data.service.support.search.PagingCriteria;

import java.util.List;

/**
 * @author Yougeshwar
 */
public interface CronTaskDataService
        extends CrudService<CronTaskConfiguration, String>
{

    List<CronTaskConfiguration> findByName(final String name);

    List<CronTaskConfiguration> findMatching(CronTaskConfigurationSearchCriteria searchCriteria,
                                             PagingCriteria pagingCriteria);
}
