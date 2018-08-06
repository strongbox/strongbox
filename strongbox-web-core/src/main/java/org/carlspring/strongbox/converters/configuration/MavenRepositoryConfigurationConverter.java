package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.MavenRepositoryConfigurationForm;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;

import org.springframework.core.convert.converter.Converter;

public enum MavenRepositoryConfigurationConverter
        implements Converter<MavenRepositoryConfigurationForm, MutableMavenRepositoryConfiguration>
{

    INSTANCE;

    @Override
    public MutableMavenRepositoryConfiguration convert(MavenRepositoryConfigurationForm mavenRepositoryConfigurationForm)
    {
        MutableMavenRepositoryConfiguration configuration = new MutableMavenRepositoryConfiguration();
        configuration.setIndexingClassNamesEnabled(mavenRepositoryConfigurationForm.isIndexingClassNamesEnabled());
        configuration.setIndexingEnabled(mavenRepositoryConfigurationForm.isIndexingEnabled());

        return configuration;
    }
}
