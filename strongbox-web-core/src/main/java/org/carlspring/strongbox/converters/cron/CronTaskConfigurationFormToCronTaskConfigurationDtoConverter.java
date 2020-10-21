package org.carlspring.strongbox.converters.cron;

import org.carlspring.strongbox.cron.domain.CronTaskConfigurationDto;
import org.carlspring.strongbox.forms.cron.CronTaskConfigurationForm;
import org.carlspring.strongbox.forms.cron.CronTaskConfigurationFormField;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
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

        List<CronTaskConfigurationFormField> fields = configurationForm.getFields();
        if (CollectionUtils.isNotEmpty(fields))
        {
            configuration.setProperties(fields.stream()
                                              .collect(Collectors.toMap(f -> f.getName(), f -> f.getValue())));
        }

        configuration.setJobClass(configurationForm.getJobClass());
        configuration.setCronExpression(configurationForm.getCronExpression());
        configuration.setOneTimeExecution(configurationForm.isOneTimeExecution());
        configuration.setImmediateExecution(configurationForm.isImmediateExecution());
        return configuration;
    }
}
