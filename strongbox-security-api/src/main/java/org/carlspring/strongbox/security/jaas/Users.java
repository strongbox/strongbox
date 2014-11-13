package org.carlspring.strongbox.security.jaas;

import javax.xml.bind.annotation.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "users")
@XmlAccessorType(XmlAccessType.FIELD)
public class Users
{

    @XmlElement(name = "user")
    private Set<User> users = new LinkedHashSet<>();


    public Users()
    {
    }

    public Users(Set<User> users)
    {
        this.users = users;
    }

    public Set<User> getUsers()
    {
        return users;
    }

    public void setUsers(Set<User> users)
    {
        this.users = users;
    }

}
