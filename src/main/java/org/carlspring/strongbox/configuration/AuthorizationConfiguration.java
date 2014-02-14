package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.jaas.Privilege;
import org.carlspring.strongbox.jaas.Role;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author mtodorov
 */
public class AuthorizationConfiguration
{

    @XStreamAlias(value = "roles")
    private List<Role> roles = new ArrayList<Role>();

    @XStreamAlias(value = "privileges")
    private List<Privilege> privileges = new ArrayList<Privilege>();


    public AuthorizationConfiguration()
    {
    }

    public List<Role> getRoles()
    {
        return roles;
    }

    public void setRoles(List<Role> roles)
    {
        this.roles = roles;
    }

    public List<Privilege> getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(List<Privilege> privileges)
    {
        this.privileges = privileges;
    }

}
