package org.carlspring.strongbox.jaas;

import java.io.Serializable;

/**
 * @author mtodorov
 */
public class Role implements Serializable
{

    private long roleId;

    private String name;

    private String description;


    public Role()
    {
    }

    public long getRoleId()
    {
        return roleId;
    }

    public void setRoleId(long roleId)
    {
        this.roleId = roleId;
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

    @Override
    public String toString()
    {
        return "Role{" +
               "roleId=" + roleId +
               ", name='" + name + '\'' +
               ", description='" + description + '\'' +
               '}';
    }

}
