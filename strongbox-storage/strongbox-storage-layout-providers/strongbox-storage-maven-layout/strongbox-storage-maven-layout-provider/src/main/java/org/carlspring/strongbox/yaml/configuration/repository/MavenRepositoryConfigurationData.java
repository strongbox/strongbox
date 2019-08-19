package org.carlspring.strongbox.yaml.configuration.repository;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.yaml.repository.CustomRepositoryConfiguration;

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
public class MavenRepositoryConfigurationData
        extends CustomRepositoryConfiguration implements MavenRepositoryConfiguration
{

    private boolean indexingEnabled;

    private boolean indexingClassNamesEnabled;

    private String downloadRemoteMavenIndexesCronExpression;

    private String rebuildMavenIndexesCronExpression;

    public MavenRepositoryConfigurationData()
    {
    }

    public MavenRepositoryConfigurationData(final MavenRepositoryConfigurationDto delegate)
    {
        this.indexingEnabled = delegate.isIndexingEnabled();
        this.indexingClassNamesEnabled = delegate.isIndexingClassNamesEnabled();
        this.downloadRemoteMavenIndexesCronExpression = delegate.getDownloadRemoteMavenIndexCronExpression();
        this.rebuildMavenIndexesCronExpression = delegate.getRebuildMavenIndexesCronExpression();
    }

    public boolean isIndexingEnabled()
    {
        return indexingEnabled;
    }

    public boolean isIndexingClassNamesEnabled()
    {
        return indexingClassNamesEnabled;
    }

    @Override
    public String getDownloadRemoteMavenIndexCronExpression()
    {
        return downloadRemoteMavenIndexesCronExpression;
    }

    @Override
    public String getRebuildMavenIndexesCronExpression()
    {
        return rebuildMavenIndexesCronExpression;
    }

}

