package org.carlspring.strongbox.yaml.configuration.repository;

import org.carlspring.strongbox.providers.layout.NpmLayoutProvider;
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
@JsonTypeName(NpmLayoutProvider.ALIAS)
public class NpmRepositoryConfigurationData
        extends CustomRepositoryConfiguration implements NpmRepositoryConfiguration
{

    private boolean allowsUnpublish;

    private String cronExpression;

    private boolean cronEnabled;

    public NpmRepositoryConfigurationData()
    {

    }

    public NpmRepositoryConfigurationData(final NpmRepositoryConfigurationDto delegate)
    {
        this.allowsUnpublish = delegate.allowsUnpublish();
        this.cronExpression = delegate.getCronExpression();
        this.cronEnabled = delegate.isCronEnabled();
    }

    public boolean isAllowsUnpublish()
    {
        return allowsUnpublish;
    }

    @Override
    public String getCronExpression()
    {
        return cronExpression;
    }

    @Override
    public boolean isCronEnabled()
    {
        return cronEnabled;
    }
}
