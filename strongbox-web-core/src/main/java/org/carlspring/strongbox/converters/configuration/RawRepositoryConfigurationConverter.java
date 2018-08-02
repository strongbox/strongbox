package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.RawRepositoryConfigurationForm;
import org.carlspring.strongbox.xml.configuration.repository.MutableRawRepositoryConfiguration;

import org.springframework.core.convert.converter.Converter;

public enum RawRepositoryConfigurationConverter
        implements Converter<RawRepositoryConfigurationForm, MutableRawRepositoryConfiguration>
{

    INSTANCE;

    @Override
    public MutableRawRepositoryConfiguration convert(RawRepositoryConfigurationForm form)
    {
        return new MutableRawRepositoryConfiguration();
    }
}
