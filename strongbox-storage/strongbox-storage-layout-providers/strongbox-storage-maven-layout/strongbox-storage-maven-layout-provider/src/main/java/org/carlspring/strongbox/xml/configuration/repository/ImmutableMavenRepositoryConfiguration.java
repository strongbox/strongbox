package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;

import javax.annotation.concurrent.Immutable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressFBWarnings(value = "AJCIP_FIELD_ISNT_FINAL_IN_IMMUTABLE_CLASS")
@JsonTypeName(Maven2LayoutProvider.ALIAS)
public class ImmutableMavenRepositoryConfiguration
        extends CustomRepositoryConfiguration implements MavenRepositoryConfiguration
{

    private boolean indexingEnabled;

    private boolean indexingClassNamesEnabled;

    ImmutableMavenRepositoryConfiguration()
    {
    }

    public ImmutableMavenRepositoryConfiguration(final MutableMavenRepositoryConfiguration delegate)
    {
        this.indexingEnabled = delegate.isIndexingEnabled();
        this.indexingClassNamesEnabled = delegate.isIndexingClassNamesEnabled();
    }

    @Override
    public boolean isIndexingEnabled()
    {
        return indexingEnabled;
    }

    @Override
    public boolean isIndexingClassNamesEnabled()
    {
        return indexingClassNamesEnabled;
    }
}

