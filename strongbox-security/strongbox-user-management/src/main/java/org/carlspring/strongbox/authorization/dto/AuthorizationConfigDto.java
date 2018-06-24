package org.carlspring.strongbox.authorization.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Java representation for authorization config that stored in XML file.
 *
 * @author Alex Oreshkevich
 * @author Pablo Tirado
 * @see /src/main/resources/etc/conf/strongbox-authorization.xml
 * @see {@linkplain https://dev.carlspring.org/youtrack/issue/SB-126}
 */
@XmlRootElement(name = "authorization-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthorizationConfigDto
        implements Serializable
{

    @XmlElement(name = "role")
    @XmlElementWrapper(name = "roles")
    private Set<RoleDto> roles = new LinkedHashSet<>();

    @XmlElement(name = "privilege")
    @XmlElementWrapper(name = "privileges")
    private Set<PrivilegeDto> privileges = new LinkedHashSet<>();

    public Set<RoleDto> getRoles()
    {
        return roles;
    }

    public void setRoles(Set<RoleDto> roles)
    {
        this.roles = roles;
    }

    public Set<PrivilegeDto> getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(final Set<PrivilegeDto> privileges)
    {
        this.privileges = privileges;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (!(o instanceof AuthorizationConfigDto))
        {
            return false;
        }
        final AuthorizationConfigDto config = (AuthorizationConfigDto) o;
        return java.util.Objects.equals(roles, config.roles) &&
               java.util.Objects.equals(privileges, config.privileges);
    }

    @Override
    public int hashCode()
    {
        return java.util.Objects.hash(roles, privileges);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("AuthorizationConfig{");
        sb.append("roles=").append(roles);
        sb.append(", privileges=").append(privileges);
        sb.append('}');
        return sb.toString();
    }
}
