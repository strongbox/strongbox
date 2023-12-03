package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.CustomRepositoryConfigurationFormVisitor;
import org.carlspring.strongbox.forms.configuration.MavenRepositoryConfigurationForm;
import org.carlspring.strongbox.forms.configuration.NugetRepositoryConfigurationForm;
import org.carlspring.strongbox.forms.configuration.RawRepositoryConfigurationForm;
import org.carlspring.strongbox.yaml.configuration.repository.MavenRepositoryConfigurationDto;
import org.carlspring.strongbox.yaml.configuration.repository.NugetRepositoryConfigurationDto;
import org.carlspring.strongbox.yaml.configuration.repository.RawRepositoryConfigurationDto;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfigurationDto;

/**
 * @author Przemyslaw Fusik
 */
public enum CustomRepositoryConfigurationFormConverter
        implements CustomRepositoryConfigurationFormVisitor<CustomRepositoryConfigurationDto>
{
    INSTANCE;

    public MavenRepositoryConfigurationDto visit(MavenRepositoryConfigurationForm form)
    {
        return MavenRepositoryConfigurationConverter.INSTANCE.convert(form);
    }

    public NugetRepositoryConfigurationDto visit(NugetRepositoryConfigurationForm form)
    {
        return NugetRepositoryConfigurationConverter.INSTANCE.convert(form);
    }

    public RawRepositoryConfigurationDto visit(RawRepositoryConfigurationForm form)
    {
        return RawRepositoryConfigurationConverter.INSTANCE.convert(form);
    }

}
