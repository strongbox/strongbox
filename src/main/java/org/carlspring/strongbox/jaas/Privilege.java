package org.carlspring.strongbox.jaas;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author mtodorov
 */
public class Privilege implements Serializable
{

    @XStreamAlias(value = "name")
    private String name;

    @XStreamAlias(value = "description")
    private String description;


    public Privilege()
    {
    }

    public Privilege(String name,
                     String description)
    {
        this.name = name;
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

}
