package org.carlspring.strongbox.yaml.configuration.repository;

import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfigurationDto;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonTypeName(NpmLayoutProvider.ALIAS)
public class NpmRepositoryConfigurationDto
        extends CustomRepositoryConfigurationDto implements NpmRepositoryConfiguration
{

    private boolean allowsUnpublish = true;

    private String cronExpression = "0 0 * ? * * ";

    private boolean cronEnabled = true;

    @Override
    public CustomRepositoryConfiguration getImmutable()
    {
        return new NpmRepositoryConfigurationData(this);
    }

    public boolean allowsUnpublish()
    {
        return allowsUnpublish;
    }

    @Override
    public String getCronExpression()
    {
        return cronExpression;
    }

    @Override
    public boolean isCronEnabled()
    {
        return cronEnabled;
    }
}
