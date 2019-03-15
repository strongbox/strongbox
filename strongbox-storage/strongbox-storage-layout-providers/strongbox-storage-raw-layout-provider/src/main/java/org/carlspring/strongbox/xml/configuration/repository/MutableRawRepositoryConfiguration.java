package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.MutableCustomRepositoryConfiguration;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonTypeName("rawRepositoryConfiguration")
public class MutableRawRepositoryConfiguration
        extends MutableCustomRepositoryConfiguration
{

    @Override
    public CustomRepositoryConfiguration getImmutable()
    {
        return new RawRepositoryConfiguration(this);
    }
}
