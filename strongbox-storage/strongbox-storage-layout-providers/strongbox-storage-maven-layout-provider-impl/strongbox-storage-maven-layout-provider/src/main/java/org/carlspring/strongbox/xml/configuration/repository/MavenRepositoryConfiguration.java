package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;

import javax.annotation.concurrent.Immutable;

/**
 * @author Przemyslaw Fusik
 */
@Immutable
public class MavenRepositoryConfiguration
        extends CustomRepositoryConfiguration
{

    private final boolean indexingEnabled;

    private final boolean indexingClassNamesEnabled;

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

