package org.carlspring.strongbox.forms.configuration;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author Przemyslaw Fusik
 */
@JsonTypeName("Maven 2")
public class MavenRepositoryConfigurationForm
        extends CustomRepositoryConfigurationForm
{

    private boolean indexingEnabled;

    private boolean indexingClassNamesEnabled;

    private String downloadRemoteMavenIndexCronExpression;

    private String rebuildMavenIndexesCronExpression;

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

    public String getDownloadRemoteMavenIndexCronExpression() { return downloadRemoteMavenIndexCronExpression; }

    public void setDownloadRemoteMavenIndexCronExpression(String downloadRemoteMavenIndexCronExpression)
    {
        this.downloadRemoteMavenIndexCronExpression = downloadRemoteMavenIndexCronExpression;
    }

    public String getRebuildMavenIndexesCronExpression() { return rebuildMavenIndexesCronExpression; }

    public void setRebuildMavenIndexesCronExpression(String rebuildMavenIndexesCronExpression)
    {
        this.rebuildMavenIndexesCronExpression = rebuildMavenIndexesCronExpression;
    }

    @Override
    public <T> T accept(final CustomRepositoryConfigurationFormVisitor<T> visitor)
    {
        return visitor.visit(this);
    }
}
