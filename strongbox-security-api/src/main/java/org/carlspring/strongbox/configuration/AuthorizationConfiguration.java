package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.security.jaas.Privilege;
import org.carlspring.strongbox.security.jaas.Role;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "authorization-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthorizationConfiguration
{

    @XmlElement(name = "roles")
    private List<Role> roles = new ArrayList<>();

    @XmlElement(name = "privileges")
    private List<Privilege> privileges = new ArrayList<>();


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
