package org.carlspring.strongbox.security.jaas;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Role implements Serializable
{

    @XmlElement
    private String name;

    @XmlElement
    private String description;

    /**
     * The repository this role is associated with.
     */
    @XmlElement
    private String repository;

    /**
     * Nested roles.
     */
    @XmlElement(name = "role")
    @XmlElementWrapper(name = "roles")
    private List<String> roles = new ArrayList<String>();

    @XmlElement(name = "privilege")
    @XmlElementWrapper(name = "privileges")
    private List<String> privileges = new ArrayList<String>();


    public Role()
    {
    }

    public Role(String name,
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

    public String getRepository()
    {
        return repository;
    }

    public void setRepository(String repository)
    {
        this.repository = repository;
    }

    public List<String> getRoles()
    {
        return roles;
    }

    public void setRoles(List<String> roles)
    {
        this.roles = roles;
    }

    public boolean addRole(String role)
    {
        return roles.add(role);
    }

    public boolean removeRole(String role)
    {
        return roles.remove(role);
    }

    public boolean containsRole(String role)
    {
        return roles.contains(role);
    }

    public List<String> getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(List<String> privileges)
    {
        this.privileges = privileges;
    }

    public boolean addPrivilege(String privilege)
    {
        return privileges.add(privilege);
    }

    public boolean removePrivilege(String privilege)
    {
        return privileges.remove(privilege);
    }

    public boolean containsPrivilege(String privilege)
    {
        return privileges.contains(privilege);
    }

    @Override
    public String toString()
    {
        return "Role{" +
               "name='" + name + '\'' +
               ", description='" + description + '\'' +
               ", repository='" + repository + '\'' +
               ", roles=" + roles +
               ", privileges=" + privileges +
               '}';
    }

}
