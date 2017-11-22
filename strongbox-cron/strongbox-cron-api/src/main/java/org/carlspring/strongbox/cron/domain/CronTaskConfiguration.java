package org.carlspring.strongbox.cron.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;
import org.carlspring.strongbox.xml.StorageMapAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;
import java.util.Map;

import org.springframework.util.Assert;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Yougeshwar
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class CronTaskConfiguration
        extends GenericEntity
{

    @XmlElement(name = "name")
    private String name;

    @XmlElement(name = "properties")
    @XmlJavaTypeAdapter(MapAdapter.class)
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
        Assert.notNull("No property of key '" + key + "' found");
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
        return new ToStringBuilder(this)
                       .append("name", name)
                       .append("properties", properties)
                       .append("oneTimeExecution", oneTimeExecution)
                       .append("immediateExecution", immediateExecution)
                       .append("uuid", uuid)
                       .toString();
    }
}
