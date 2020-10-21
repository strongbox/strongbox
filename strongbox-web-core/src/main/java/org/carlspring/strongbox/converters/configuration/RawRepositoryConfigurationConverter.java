package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.RawRepositoryConfigurationForm;
import org.carlspring.strongbox.yaml.configuration.repository.RawRepositoryConfigurationDto;

import org.springframework.core.convert.converter.Converter;

public enum RawRepositoryConfigurationConverter
        implements Converter<RawRepositoryConfigurationForm, RawRepositoryConfigurationDto>
{

    INSTANCE;

    @Override
    public RawRepositoryConfigurationDto convert(RawRepositoryConfigurationForm form)
    {
        return new RawRepositoryConfigurationDto();
    }
}
