package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.MavenRepositoryConfigurationForm;
import org.carlspring.strongbox.yaml.configuration.repository.MavenRepositoryConfigurationDto;

import org.springframework.core.convert.converter.Converter;

public enum MavenRepositoryConfigurationConverter
        implements Converter<MavenRepositoryConfigurationForm, MavenRepositoryConfigurationDto>
{

    INSTANCE;

    @Override
    public MavenRepositoryConfigurationDto convert(MavenRepositoryConfigurationForm mavenRepositoryConfigurationForm)
    {
        MavenRepositoryConfigurationDto configuration = new MavenRepositoryConfigurationDto();
        configuration.setIndexingClassNamesEnabled(mavenRepositoryConfigurationForm.isIndexingClassNamesEnabled());
        configuration.setIndexingEnabled(mavenRepositoryConfigurationForm.isIndexingEnabled());
        configuration.setCronExpression(mavenRepositoryConfigurationForm.getCronExpression());
        configuration.setMetadataExpirationStrategy(mavenRepositoryConfigurationForm.getMetadataExpirationStrategy());

        return configuration;
    }
}
