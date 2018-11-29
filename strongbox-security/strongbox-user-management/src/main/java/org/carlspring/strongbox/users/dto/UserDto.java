package org.carlspring.strongbox.users.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mtodorov
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserDto
        implements Serializable, UserReadContract
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

    @XmlElement(name = "authority")
    @XmlElementWrapper(name = "authorities")
    private Set<String> authorities = new HashSet<>();

    @XmlElement(name = "access-model", type = UserAccessModelDto.class)
    private UserAccessModelReadContract userAccessModel;

    @XmlElement(name = "security-token-key")
    private String securityTokenKey;

    @XmlTransient
    private Date lastUpdate;
    
    public UserDto()
    {
    }

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserReadContract#getUsername()
     */
    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserReadContract#getPassword()
     */
    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserReadContract#getRoles()
     */
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

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserReadContract#getAuthorities()
     */
    public Set<String> getAuthorities()
    {
        return authorities;
    }

    public void setAuthorities(Set<String> authorities)
    {
        this.authorities = authorities;
    }

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserReadContract#getUserAccessModel()
     */
    public UserAccessModelReadContract getUserAccessModel()
    {
        return userAccessModel;
    }

    public void setUserAccessModel(UserAccessModelReadContract userAccessModel)
    {
        this.userAccessModel = userAccessModel;
    }

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserReadContract#getSecurityTokenKey()
     */
    public String getSecurityTokenKey()
    {
        return securityTokenKey;
    }

    public void setSecurityTokenKey(String securityTokenKey)
    {
        this.securityTokenKey = securityTokenKey;
    }

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserReadContract#isEnabled()
     */
    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(final boolean enabled)
    {
        this.enabled = enabled;
    }

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.users.dto.UserReadContract#getLastUpdate()
     */
    public Date getLastUpdate()
    {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate)
    {
        this.lastUpdate = lastUpdate;
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
