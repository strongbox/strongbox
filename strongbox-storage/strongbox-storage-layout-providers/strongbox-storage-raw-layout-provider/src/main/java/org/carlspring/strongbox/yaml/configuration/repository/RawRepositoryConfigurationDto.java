package org.carlspring.strongbox.yaml.configuration.repository;

import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfigurationDto;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonTypeName(RawLayoutProvider.ALIAS)
public class RawRepositoryConfigurationDto
        extends CustomRepositoryConfigurationDto
{

    @Override
    public CustomRepositoryConfiguration getImmutable()
    {
        return new RawRepositoryConfigurationData(this);
    }
}
