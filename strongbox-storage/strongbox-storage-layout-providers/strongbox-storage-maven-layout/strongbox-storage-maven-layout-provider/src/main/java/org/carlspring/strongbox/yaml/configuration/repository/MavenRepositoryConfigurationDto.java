package org.carlspring.strongbox.yaml.configuration.repository;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfiguration;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfigurationDto;

import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author carlspring
 * @author Pablo Tirado
 */
@JsonTypeName(Maven2LayoutProvider.ALIAS)
public class MavenRepositoryConfigurationDto
        extends CustomRepositoryConfigurationDto implements MavenRepositoryConfiguration
{

    private boolean indexingEnabled = false;

    private boolean indexingClassNamesEnabled = true;

    // defaults to once daily at midnight
    private String downloadRemoteMavenIndexCronExpression = "0 0 0 * * ?";

    // defaults to once daily at 2 am
    private String rebuildMavenIndexesCronExpression = "0 0 2 * * ?";

    @Override
    public boolean isIndexingEnabled()
    {
        return indexingEnabled;
    }

    public void setIndexingEnabled(boolean indexingEnabled)
    {
        this.indexingEnabled = indexingEnabled;
    }

    @Override
    public boolean isIndexingClassNamesEnabled()
    {
        return indexingClassNamesEnabled;
    }

    public void setIndexingClassNamesEnabled(final boolean indexingClassNamesEnabled)
    {
        this.indexingClassNamesEnabled = indexingClassNamesEnabled;
    }

    @Override
    public String getDownloadRemoteMavenIndexCronExpression() { return downloadRemoteMavenIndexCronExpression; }

    public void setDownloadRemoteMavenIndexCronExpression(String downloadRemoteMavenIndexCronExpression)
    {
        this.downloadRemoteMavenIndexCronExpression = downloadRemoteMavenIndexCronExpression;
    }

    @Override
    public String getRebuildMavenIndexesCronExpression() { return rebuildMavenIndexesCronExpression; }

    public void setRebuildMavenIndexesCronExpression(String rebuildMavenIndexesCronExpression)
    {
        this.rebuildMavenIndexesCronExpression = rebuildMavenIndexesCronExpression;
    }

    @Override
    public CustomRepositoryConfiguration getImmutable()
    {
        return new MavenRepositoryConfigurationData(this);
    }
}
