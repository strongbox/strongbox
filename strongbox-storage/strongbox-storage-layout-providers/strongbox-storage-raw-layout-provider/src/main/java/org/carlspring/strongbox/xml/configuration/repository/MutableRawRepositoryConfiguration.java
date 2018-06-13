package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.MutableCustomRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carlspring
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "raw-repository-configuration")
public class MutableRawRepositoryConfiguration
        extends MutableCustomRepositoryConfiguration
{


    public MutableRawRepositoryConfiguration()
    {
    }

    @Override
    public CustomRepositoryConfiguration getImmutable()
    {
        return new RawRepositoryConfiguration(this);
    }
}
