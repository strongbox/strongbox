package org.carlspring.strongbox.xml.configuration.repository;

import org.carlspring.strongbox.xml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.xml.repository.MutableCustomRepositoryConfiguration;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonRootName("mavenRepositoryConfiguration")
public class MutableMavenRepositoryConfiguration
        extends MutableCustomRepositoryConfiguration
{

    private boolean indexingEnabled = false;

    private boolean indexingClassNamesEnabled = true;

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
