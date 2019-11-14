package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.storage.metadata.maven.MetadataExpirationStrategyType;
import org.carlspring.strongbox.validation.configuration.DescribableEnumValue;

import javax.validation.constraints.Pattern;

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

    private String cronExpression;

    @DescribableEnumValue(type = MetadataExpirationStrategyType.class,
            message = "metadataExpirationStrategy must be equal to either:  checksum, refresh")
    private String metadataExpirationStrategy;

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

    @Override
    public <T> T accept(final CustomRepositoryConfigurationFormVisitor<T> visitor)
    {
        return visitor.visit(this);
    }
}
