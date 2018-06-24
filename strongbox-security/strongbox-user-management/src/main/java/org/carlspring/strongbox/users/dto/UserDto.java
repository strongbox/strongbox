package org.carlspring.strongbox.users.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mtodorov
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserDto
        implements Serializable
{

    @XmlElement
    private String username;

    @XmlElement
    private String password;

    @XmlElement
    private boolean enabled = true;

    @XmlElement(name = "role")
    @XmlElementWrapper(name = "roles")
    private Set<String> roles = new HashSet<>();

    @XmlElement(name = "access-model")
    private UserAccessModelDto userAccessModel;

    @XmlElement(name = "security-token-key")
    private String securityTokenKey;

    public UserDto()
    {
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public Set<String> getRoles()
    {
        return roles;
    }

    public void setRoles(Set<String> roles)
    {
        this.roles = roles != null ? new HashSet<>(roles) : new HashSet<>();
    }

    public void addRole(String role)
    {
        roles.add(role);
    }

    public void removeRole(String role)
    {
        roles.remove(role);
    }

    public boolean hasRole(String role)
    {
        return roles.contains(role);
    }

    public UserAccessModelDto getUserAccessModel()
    {
        return userAccessModel;
    }

    public void setUserAccessModel(UserAccessModelDto userAccessModel)
    {
        this.userAccessModel = userAccessModel;
    }

    public String getSecurityTokenKey()
    {
        return securityTokenKey;
    }

    public void setSecurityTokenKey(String securityTokenKey)
    {
        this.securityTokenKey = securityTokenKey;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(final boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("username='")
          .append(username)
          .append('\'');
        sb.append(", roles=")
          .append(roles);
        sb.append(", userAccessModel=")
          .append(userAccessModel);
        sb.append('}');
        return sb.toString();
    }
}
