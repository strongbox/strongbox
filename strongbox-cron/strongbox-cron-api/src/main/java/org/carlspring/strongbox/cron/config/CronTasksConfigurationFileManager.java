package org.carlspring.strongbox.cron.config;

import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.xml.XmlFileManager;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class CronTasksConfigurationFileManager
        extends XmlFileManager<CronTasksConfigurationDto>
{

    @Override
    public String getPropertyKey()
    {
        return "strongbox.cron.tasks.xml";
    }

    @Override
    public String getDefaultLocation()
    {
        return "etc/conf/strongbox-cron-tasks.xml";
    }

}
