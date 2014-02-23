package org.carlspring.strongbox.security.jaas;

import java.io.Serializable;
import java.util.*;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * @author mtodorov
 */
public class User implements Serializable
{

    @XStreamOmitField
    private long userId;

    @XStreamAlias(value = "username")
    private String username;

    @XStreamAlias(value = "credentials")
    private Credentials credentials = new Credentials();

    @XStreamAlias(value = "roles")
    private List<String> roles = new ArrayList<String>();

    @XStreamAlias(value = "firstName")
    private String firstName;

    @XStreamAlias(value = "lastName")
    private String lastName;

    @XStreamAlias(value = "email")
    private String email;

    @XStreamOmitField
    private long seed;


    public User()
    {
    }

    public User(String username,
                Credentials credentials)
    {
        this.username = username;
        this.credentials = credentials;
    }

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
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

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
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

    public List<String> getRoles()
    {
        return roles;
    }

    public void setRoles(List<String> roles)
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

    @Override
    public String toString()
    {
        return "User{" +
               "userId=" + userId +
               ", username='" + getUsername() + '\'' +
               ", password='" + getPassword() + '\'' +
               (roles != null ? ", roles=" + Arrays.toString(roles.toArray()) : "") +
               '}';
    }

}
