package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.NugetRepositoryConfigurationForm;
import org.carlspring.strongbox.yaml.configuration.repository.NugetRepositoryConfigurationDto;

import org.springframework.core.convert.converter.Converter;

public enum NugetRepositoryConfigurationConverter
        implements Converter<NugetRepositoryConfigurationForm, NugetRepositoryConfigurationDto>
{

    INSTANCE;

    @Override
    public NugetRepositoryConfigurationDto convert(NugetRepositoryConfigurationForm form)
    {
        NugetRepositoryConfigurationDto configuration = new NugetRepositoryConfigurationDto();
        configuration.setFeedVersion(form.getFeedVersion());
        configuration.setRemoteFeedPageSize(form.getRemoteFeedPageSize());

        return configuration;
    }
}
