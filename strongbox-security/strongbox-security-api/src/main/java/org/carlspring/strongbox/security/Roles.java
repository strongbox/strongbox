package org.carlspring.strongbox.security;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author mtodorov
 */
@Entity
@XmlRootElement(name = "roles")
@XmlAccessorType(XmlAccessType.FIELD)
public class Roles
{

    @XmlElement(name = "role")
    @Embedded
    private Set<Role> roles = new LinkedHashSet<>();

    public Roles()
    {
    }

    public Roles(Set<Role> roles)
    {
        this.roles = roles;
    }

    public Set<Role> getRoles()
    {
        return roles;
    }

    public void setRoles(Set<Role> roles)
    {
        this.roles = roles;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("\n\tRoles{");
        sb.append("roles=").append(roles);
        sb.append('}');

        return sb.toString();
    }
}
