package org.carlspring.strongbox.security;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author mtodorov
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class User
        implements Serializable
{

    @XmlElement
    private String username;

    @XmlElement
    private String password;

    @XmlElement(name = "role")
    @XmlElementWrapper(name = "roles")
    private Set<String> roles = new HashSet<>();

    @XmlElement
    private String fullName;

    @XmlElement
    private String email;

    @XmlElement(name = "access-model")
    private UserAccessModel userAccessModel;

    @XmlElement(name = "security-token-key")
    private String securityTokenKey;

    public User()
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

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public Set<String> getRoles()
    {
        return roles;
    }

    public void setRoles(Set<String> roles)
    {
        this.roles = roles;
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

    public UserAccessModel getUserAccessModel()
    {
        return userAccessModel;
    }

    public void setUserAccessModel(UserAccessModel userAccessModel)
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

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("username='")
          .append(username)
          .append('\'');
        sb.append(", roles=")
          .append(roles);
        sb.append(", fullName='")
          .append(fullName)
          .append('\'');
        sb.append(", email='")
          .append(email)
          .append('\'');
        sb.append(", userAccessModel=")
          .append(userAccessModel);
        sb.append('}');
        return sb.toString();
    }
}
