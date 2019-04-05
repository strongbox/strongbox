package org.carlspring.strongbox.yaml.configuration.repository;

import org.carlspring.strongbox.providers.layout.RawLayoutProvider;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.MutableCustomRepositoryConfiguration;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonTypeName(RawLayoutProvider.ALIAS)
public class MutableRawRepositoryConfiguration
        extends MutableCustomRepositoryConfiguration
{

    @Override
    public CustomRepositoryConfiguration getImmutable()
    {
        return new RawRepositoryConfiguration(this);
    }
}
