package org.carlspring.strongbox.security;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.carlspring.strongbox.data.domain.GenericEntity;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "privileges")
@XmlAccessorType(XmlAccessType.FIELD)
public class Privileges
        extends GenericEntity
{

    @XmlElement(name = "privilege")
    @OneToMany(cascade = CascadeType.ALL)
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

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("\n\tPrivileges{");
        sb.append("privileges=").append(privileges);
        sb.append('}');
        
        return sb.toString();
    }
    
}
