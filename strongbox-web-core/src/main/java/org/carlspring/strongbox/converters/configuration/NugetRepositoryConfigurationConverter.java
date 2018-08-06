package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.NugetRepositoryConfigurationForm;
import org.carlspring.strongbox.xml.configuration.repository.MutableNugetRepositoryConfiguration;

import org.springframework.core.convert.converter.Converter;

public enum NugetRepositoryConfigurationConverter
        implements Converter<NugetRepositoryConfigurationForm, MutableNugetRepositoryConfiguration>
{

    INSTANCE;

    @Override
    public MutableNugetRepositoryConfiguration convert(NugetRepositoryConfigurationForm form)
    {
        MutableNugetRepositoryConfiguration configuration = new MutableNugetRepositoryConfiguration();
        configuration.setFeedVersion(form.getFeedVersion());
        configuration.setRemoteFeedPageSize(form.getRemoteFeedPageSize());

        return configuration;
    }
}
