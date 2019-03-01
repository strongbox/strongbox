package org.carlspring.strongbox.cron.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.springframework.util.Assert;

/**
 * @author Yougeshwar
 * @author Pablo Tirado
 */
public class CronTaskConfigurationDto
        implements Serializable
{

    private String uuid;

    private String name;

    private Map<String, String> properties = new HashMap<>();

    private boolean oneTimeExecution = false;

    private boolean immediateExecution = false;

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

    public String getRequiredProperty(String key)
    {
        String value = getProperty(key);
        Assert.notNull(value, "No property of key '" + key + "' found");
        return value;
    }

    public String getProperty(String key)
    {
        return this.properties.get(key);
    }

    public void addProperty(String key,
                            String value)
    {
        properties.put(key, value);
    }

    public boolean contains(String key)
    {
        return properties.containsKey(key);
    }

    public boolean isOneTimeExecution()
    {
        return oneTimeExecution;
    }

    public void setOneTimeExecution(boolean oneTimeExecution)
    {
        this.oneTimeExecution = oneTimeExecution;
    }

    @JsonGetter("immediateExecution")
    public boolean shouldExecuteImmediately()
    {
        return immediateExecution;
    }

    public void setImmediateExecution(boolean immediateExecution)
    {
        this.immediateExecution = immediateExecution;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("CronTaskConfiguration{");
        sb.append("uuid='").append(uuid).append('\'');
        sb.append("name='").append(name).append('\'');
        sb.append(", properties=").append(properties);
        sb.append(", oneTimeExecution=").append(oneTimeExecution);
        sb.append(", immediateExecution=").append(immediateExecution);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof CronTaskConfigurationDto))
        {
            return false;
        }

        final CronTaskConfigurationDto that = (CronTaskConfigurationDto) o;

        return getUuid() != null ? getUuid().equals(that.getUuid()) : that.getUuid() == null;
    }

    @Override
    public int hashCode()
    {
        return getUuid() != null ? getUuid().hashCode() : 0;
    }
}
