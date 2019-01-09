package org.carlspring.strongbox.forms.cron;

import org.carlspring.strongbox.validation.cron.UniqueCronTaskConfiguration;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Maps;

/**
 * @author Pablo Tirado
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CronTaskConfigurationForm
        implements Serializable
{

    @NotEmpty(message = "Configuration UUID is required!")
    @UniqueCronTaskConfiguration(groups = NewConfiguration.class, message = "Configuration UUID is already taken.")
    private String uuid;

    @NotEmpty(message = "Configuration name is required!")
    private String name;

    private Map<String, String> properties = Maps.newHashMap();

    private boolean oneTimeExecution;

    private boolean immediateExecution;


    public String getUuid()
    {
        return uuid;
    }

    public void setUuid(String uuid)
    {
        this.uuid = uuid;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Map<String, String> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    public void addProperty(String key,
                            String value)
    {
        this.properties.put(key, value);
    }
    public boolean isOneTimeExecution()
    {
        return oneTimeExecution;
    }

    public void setOneTimeExecution(boolean oneTimeExecution)
    {
        this.oneTimeExecution = oneTimeExecution;
    }

    public boolean isImmediateExecution()
    {
        return immediateExecution;
    }

    public void setImmediateExecution(boolean immediateExecution)
    {
        this.immediateExecution = immediateExecution;
    }

    public interface NewConfiguration
            extends Serializable
    {
        // validation group marker interface for new configurations.
    }

    public interface ExistingConfiguration
            extends Serializable
    {
        // validation group marker interface for existing configurations.
    }
}
