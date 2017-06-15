package org.carlspring.strongbox.security;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.persistence.Embedded;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "roles")
@XmlAccessorType(XmlAccessType.FIELD)
public class Roles
        extends GenericEntity
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
