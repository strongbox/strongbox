package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.providers.layout.Maven2LayoutProvider;
import org.carlspring.strongbox.storage.metadata.maven.MetadataExpirationStrategyType;
import org.carlspring.strongbox.validation.configuration.DescribableEnumValue;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Set;

/**
 * @author Przemyslaw Fusik
 */
@JsonTypeName(Maven2LayoutProvider.ALIAS)
public class MavenRepositoryConfigurationForm
        extends CustomRepositoryConfigurationForm
{

    private boolean indexingEnabled;

    private boolean indexingClassNamesEnabled;

    private String cronExpression;

    @DescribableEnumValue(type = MetadataExpirationStrategyType.class,
                          message = "The metadataExpirationStrategy can be either \"checksum\", or \"refresh\"")
    private String metadataExpirationStrategy;

    private Set<String> digestAlgorithmSet;

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

    public String getCronExpression() { return cronExpression; }

    public void setCronExpression(String cronExpression)
    {
        this.cronExpression = cronExpression;
    }

    public String getMetadataExpirationStrategy()
    {
        return metadataExpirationStrategy;
    }

    public void setMetadataExpirationStrategy(String metadataExpirationStrategy)
    {
        this.metadataExpirationStrategy = metadataExpirationStrategy;
    }

    public Set<String> getDigestAlgorithmSet()
    {
        return digestAlgorithmSet;
    }

    public void setDigestAlgorithmSet(Set<String> digestAlgorithmSet)
    {
        this.digestAlgorithmSet = digestAlgorithmSet;
    }

    @Override
    public <T> T accept(final CustomRepositoryConfigurationFormVisitor<T> visitor)
    {
        return visitor.visit(this);
    }

}
