package org.carlspring.strongbox.security.jaas;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "privileges")
@XmlAccessorType(XmlAccessType.FIELD)
public class Privileges
{

    @XmlElement(name = "privilege")
    private Set<Privilege> privileges = new LinkedHashSet<>();


    public Privileges()
    {
    }

    public Privileges(Set<Privilege> privileges)
    {
        this.privileges = privileges;
    }

    public Set<Privilege> getPrivileges()
    {
        return privileges;
    }

    public void setPrivileges(Set<Privilege> privileges)
    {
        this.privileges = privileges;
    }

}
