package org.carlspring.strongbox.forms.configuration;

/**
 * @author Przemyslaw Fusik
 */
public class MavenRepositoryConfigurationForm
        extends CustomRepositoryConfigurationForm
{

    private boolean indexingEnabled;

    private boolean indexingClassNamesEnabled;

    public boolean isIndexingEnabled()
    {
        return indexingEnabled;
    }

    public void setIndexingEnabled(final boolean indexingEnabled)
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
}
