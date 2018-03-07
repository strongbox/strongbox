package org.carlspring.strongbox.cron.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;
import org.carlspring.strongbox.xml.PropertiesAdapter;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

/**
 * @author Yougeshwar
 */
@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CronTaskConfiguration
        extends GenericEntity
{

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "properties")
    @XmlJavaTypeAdapter(PropertiesAdapter.class)
    private Map<String, String> properties = new HashMap<>();

    @XmlElement(name = "one-time-execution")
    private boolean oneTimeExecution = false;

    @XmlElement(name = "immediate-execution")
    private boolean immediateExecution = false;


    public CronTaskConfiguration()
    {
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

    public void removeProperty(String key)
    {
        properties.remove(key);
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
        sb.append("name='").append(name).append('\'');
        sb.append(", properties=").append(properties);
        sb.append(", oneTimeExecution=").append(oneTimeExecution);
        sb.append(", immediateExecution=").append(immediateExecution);
        sb.append(", uuid='").append(uuid).append('\'');
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
        if (!(o instanceof CronTaskConfiguration))
        {
            return false;
        }

        final CronTaskConfiguration that = (CronTaskConfiguration) o;

        return getUuid() != null ? getUuid().equals(that.getUuid()) : that.getUuid() == null;
    }

    @Override
    public int hashCode()
    {
        return getUuid() != null ? getUuid().hashCode() : 0;
    }
}
