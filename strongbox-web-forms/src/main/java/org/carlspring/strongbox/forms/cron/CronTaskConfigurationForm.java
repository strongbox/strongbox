package org.carlspring.strongbox.forms.cron;

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

    @NotEmpty(message = "Configuration name is required!")
    private String name;

    private Map<String, String> properties = Maps.newHashMap();

    private boolean oneTimeExecution;

    private boolean immediateExecution;

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
}
