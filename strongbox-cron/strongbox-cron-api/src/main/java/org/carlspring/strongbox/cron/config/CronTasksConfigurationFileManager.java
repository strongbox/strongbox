package org.carlspring.strongbox.cron.config;

import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.resource.ConfigurationResourceResolver;
import org.carlspring.strongbox.yaml.YamlFileManager;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@Component
public class CronTasksConfigurationFileManager
        extends YamlFileManager<CronTasksConfigurationDto>
{

    @Inject
    private ConfigurationResourceResolver configurationResourceResolver;

    @Override
    public String getPropertyKey()
    {
        return "strongbox.cron.tasks.yaml";
    }

    @Override
    public String getDefaultLocation()
    {
        return "etc/conf/strongbox-cron-tasks.yaml";
    }

    @Override
    public ConfigurationResourceResolver getConfigurationResourceResolver()
    {
        return configurationResourceResolver;
    }

}
