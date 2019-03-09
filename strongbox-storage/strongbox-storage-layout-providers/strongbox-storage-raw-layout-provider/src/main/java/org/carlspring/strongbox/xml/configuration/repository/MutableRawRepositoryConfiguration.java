package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.MutableCustomRepositoryConfiguration;

/**
 * @author carlspring
 */
public class MutableRawRepositoryConfiguration
        extends MutableCustomRepositoryConfiguration
{

    @Override
    public CustomRepositoryConfiguration getImmutable()
    {
        return new RawRepositoryConfiguration(this);
    }
}
