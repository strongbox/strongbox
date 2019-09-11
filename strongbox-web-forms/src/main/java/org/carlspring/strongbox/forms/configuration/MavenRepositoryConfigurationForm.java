package org.carlspring.strongbox.forms.configuration;

import org.carlspring.strongbox.providers.datastore.StorageProviderEnum;
import org.carlspring.strongbox.storage.repository.MetadataStrategyEnum;
import org.carlspring.strongbox.validation.configuration.DescribableEnumValue;

import javax.validation.constraints.NotEmpty;

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

    @NotEmpty(message = "A metadata strategy must be specified.")
    @DescribableEnumValue(message = "An metadata strategy is invalid.", type = MetadataStrategyEnum.class)
    private String metadataStrategy;

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

    public String getMetadataStrategy()
    {
        return metadataStrategy;
    }

    public void setMetadataStrategy(String metadataStrategy)
    {
        this.metadataStrategy = metadataStrategy;
    }

    @Override
    public <T> T accept(final CustomRepositoryConfigurationFormVisitor<T> visitor)
    {
        return visitor.visit(this);
    }
}
