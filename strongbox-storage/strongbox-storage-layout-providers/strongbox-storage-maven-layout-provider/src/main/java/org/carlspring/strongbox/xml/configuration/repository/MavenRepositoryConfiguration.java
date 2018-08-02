package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@JsonTypeName(Maven2LayoutProvider.ALIAS)
public class MavenRepositoryConfiguration
        extends CustomRepositoryConfiguration
{

    private boolean indexingEnabled;

    private boolean indexingClassNamesEnabled;

    MavenRepositoryConfiguration()
    {
    }

    public MavenRepositoryConfiguration(final MutableMavenRepositoryConfiguration delegate)
    {
        this.indexingEnabled = delegate.isIndexingEnabled();
        this.indexingClassNamesEnabled = delegate.isIndexingClassNamesEnabled();
    }

    public boolean isIndexingEnabled()
    {
        return indexingEnabled;
    }

    public boolean isIndexingClassNamesEnabled()
    {
        return indexingClassNamesEnabled;
    }
}

