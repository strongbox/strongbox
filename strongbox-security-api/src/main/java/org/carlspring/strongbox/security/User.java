package org.carlspring.strongbox.security;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
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
    private Credentials credentials = new Credentials();

    @XmlElement(name = "role")
    @XmlElementWrapper(name = "roles")
    private Set<String> roles = new HashSet<>();

    @XmlElement
    private String fullName;

    @XmlElement
    private String email;

    @XmlTransient
    private long seed;

    @XmlElement
    private Features features;

    public User()
    {
    }

    public User(String username,
                Credentials credentials)
    {
        this.username = username;
        this.credentials = credentials;
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
        return credentials.getPassword();
    }

    public void setPassword(String password)
    {
        credentials.setPassword(password);
    }

    public Credentials getCredentials()
    {
        return credentials;
    }

    public void setCredentials(Credentials credentials)
    {
        this.credentials = credentials;
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

    public long getSeed()
    {
        return seed;
    }

    public void setSeed(long seed)
    {
        this.seed = seed;
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

    public Features getFeatures()
    {
        return features;
    }

    public void setFeatures(Features features)
    {
        this.features = features;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("username='")
          .append(username)
          .append('\'');
        sb.append(", credentials=")
          .append(credentials);
        sb.append(", roles=")
          .append(roles);
        sb.append(", fullName='")
          .append(fullName)
          .append('\'');
        sb.append(", email='")
          .append(email)
          .append('\'');
        sb.append(", seed=")
          .append(seed);
        sb.append(", features=")
          .append(features);
        sb.append('}');
        return sb.toString();
    }
}
