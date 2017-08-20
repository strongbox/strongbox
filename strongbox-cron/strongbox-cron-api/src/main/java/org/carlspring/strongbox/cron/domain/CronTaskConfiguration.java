package org.carlspring.strongbox.cron.domain;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

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

    public boolean contain(String key)
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

}
