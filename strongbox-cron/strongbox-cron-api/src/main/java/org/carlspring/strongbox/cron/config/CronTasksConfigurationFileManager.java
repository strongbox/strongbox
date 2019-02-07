package org.carlspring.strongbox.cron.config;

import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.xml.XmlFileManager;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 */
@Component
public class CronTasksConfigurationFileManager
        extends XmlFileManager<CronTasksConfigurationDto>
{

    @Inject
    private ConfigurationResourceResolver configurationResourceResolver;

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

    @Override
    public ConfigurationResourceResolver getConfigurationResourceResolver()
    {
        return configurationResourceResolver;
    }

}
