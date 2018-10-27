package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.MutableCustomRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author carlspring
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "maven-repository-configuration")
public class MutableMavenRepositoryConfiguration
        extends MutableCustomRepositoryConfiguration
{

    @XmlAttribute(name = "indexing-enabled")
    private boolean indexingEnabled = false;

    @XmlAttribute(name = "indexing-class-names-enabled")
    private boolean indexingClassNamesEnabled = true;


    public MutableMavenRepositoryConfiguration()
    {
    }

    public boolean isIndexingEnabled()
    {
        return indexingEnabled;
    }

    public void setIndexingEnabled(boolean indexingEnabled)
    {
        this.indexingEnabled = indexingEnabled;
    }

    public boolean isIndexingClassNamesEnabled()
    {
        return indexingClassNamesEnabled;
    }

    public void setIndexingClassNamesEnabled(final boolean indexingClassNamesEnabled)
    {
        this.indexingClassNamesEnabled = indexingClassNamesEnabled;
    }

    @Override
    public CustomRepositoryConfiguration getImmutable()
    {
        return new MavenRepositoryConfiguration(this);
    }
}
