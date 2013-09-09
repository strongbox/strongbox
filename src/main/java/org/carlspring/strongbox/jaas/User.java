package org.carlspring.strongbox.jaas;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author mtodorov
 */
public class User implements Serializable
{

    private long userId;

    private String username;

    private Credentials credentials = new Credentials();

    private Set<Role> roles = new LinkedHashSet<Role>();


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

    public Set<Role> getRoles()
    {
        return roles;
    }

    public void setRoles(Set<Role> roles)
    {
        this.roles = roles;
    }

    public boolean hasRole(String roleName)
    {
        for (Role role : roles)
        {
            if (role.getName().equals(roleName))
                return true;
        }

        return false;
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
