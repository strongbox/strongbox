package org.carlspring.strongbox.converters.configuration;

import org.carlspring.strongbox.forms.configuration.CustomRepositoryConfigurationFormVisitor;
import org.carlspring.strongbox.forms.configuration.MavenRepositoryConfigurationForm;
import org.carlspring.strongbox.forms.configuration.NugetRepositoryConfigurationForm;
import org.carlspring.strongbox.forms.configuration.RawRepositoryConfigurationForm;
import org.carlspring.strongbox.xml.configuration.repository.MutableMavenRepositoryConfiguration;
import org.carlspring.strongbox.xml.configuration.repository.MutableNugetRepositoryConfiguration;
import org.carlspring.strongbox.xml.configuration.repository.MutableRawRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.MutableCustomRepositoryConfiguration;

/**
 * @author Przemyslaw Fusik
 */
public enum CustomRepositoryConfigurationFormConverter
        implements CustomRepositoryConfigurationFormVisitor<MutableCustomRepositoryConfiguration>
{
    INSTANCE;

    public MutableMavenRepositoryConfiguration visit(MavenRepositoryConfigurationForm form)
    {
        return MavenRepositoryConfigurationConverter.INSTANCE.convert(form);
    }

    public MutableNugetRepositoryConfiguration visit(NugetRepositoryConfigurationForm form)
    {
        return NugetRepositoryConfigurationConverter.INSTANCE.convert(form);
    }

    public MutableRawRepositoryConfiguration visit(RawRepositoryConfigurationForm form)
    {
        return RawRepositoryConfigurationConverter.INSTANCE.convert(form);
    }

}
