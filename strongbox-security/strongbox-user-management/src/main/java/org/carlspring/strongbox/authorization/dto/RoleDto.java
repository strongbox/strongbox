package org.carlspring.strongbox.authorization.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Objects;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "role")
@XmlAccessorType(XmlAccessType.FIELD)
public class RoleDto
        implements Serializable
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

    @XmlElement(name = "privilege")
    @XmlElementWrapper(name = "privileges")
    private Set<String> privileges = new HashSet<>();


    public RoleDto()
    {
    }

    public RoleDto(String name,
                   String description)
    {
        this.name = name;
        this.description = description;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        RoleDto role = (RoleDto) o;
        return Objects.equal(name, role.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(name);
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

    public Set<String> getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(Set<String> privileges)
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
        final StringBuilder sb = new StringBuilder("\n\t\tRole{");
        sb.append("name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", repository='").append(repository).append('\'');
        sb.append(", privileges=").append(privileges);
        sb.append('}');

        return sb.toString();
    }

}
