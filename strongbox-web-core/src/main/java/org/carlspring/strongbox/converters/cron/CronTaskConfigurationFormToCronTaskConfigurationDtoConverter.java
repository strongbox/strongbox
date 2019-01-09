package org.carlspring.strongbox.converters.cron;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.forms.cron.CronTaskConfigurationForm;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public enum CronTaskConfigurationFormToCronTaskConfigurationDtoConverter
        implements Converter<CronTaskConfigurationForm, CronTaskConfigurationDto>
{

    INSTANCE;

    @Override
    public CronTaskConfigurationDto convert(CronTaskConfigurationForm configurationForm)
    {
        CronTaskConfigurationDto configuration = new CronTaskConfigurationDto();
        configuration.setUuid(configurationForm.getUuid());
        configuration.setName(configurationForm.getName());
        configuration.setProperties(configurationForm.getProperties());
        configuration.setOneTimeExecution(configurationForm.isOneTimeExecution());
        configuration.setImmediateExecution(configurationForm.isImmediateExecution());
        return configuration;
    }
}
