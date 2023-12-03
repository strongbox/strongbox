package org.carlspring.strongbox.cron.config;

import org.carlspring.strongbox.cron.domain.CronTasksConfigurationDto;
import org.carlspring.strongbox.yaml.YAMLMapperFactory;
import org.carlspring.strongbox.yaml.YamlFileManager;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author Przemyslaw Fusik
 * @author Pablo Tirado
 */
@Component
public class CronTasksConfigurationFileManager
        extends YamlFileManager<CronTasksConfigurationDto>
{
    @Value("#{@propertiesPathResolver.resolve('strongbox.cron.tasks.yaml','etc/conf/strongbox-cron-tasks.yaml')}")
    private Resource resource;

    @Inject
    public CronTasksConfigurationFileManager(YAMLMapperFactory yamlMapperFactory)
    {
        super(yamlMapperFactory);
    }

    @Override
    protected Resource getResource()
    {
        return resource;
    }
}
