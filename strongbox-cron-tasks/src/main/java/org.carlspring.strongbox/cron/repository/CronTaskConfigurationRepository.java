package org.carlspring.strongbox.cron.repository;

import org.carlspring.strongbox.cron.domain.CronTaskConfiguration;
import org.carlspring.strongbox.data.repository.OrientRepository;

import org.springframework.transaction.annotation.Transactional;

/**
 * @author Yougeshwar
 */
@Transactional
public interface CronTaskConfigurationRepository
        extends OrientRepository<CronTaskConfiguration>
{

    CronTaskConfiguration findByName(String name);
}
