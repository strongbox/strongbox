package org.carlspring.strongbox.security.jaas;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;

/**
 * @author mtodorov
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Privilege
        extends GenericEntity
{

    @XmlElement
    private String name;

    @XmlElement
    private String description;


    public Privilege()
    {
    }

    public Privilege(String name,
                     String description)
    {
        this.name = name;
        this.description = description;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Privilege privilege = (Privilege) o;
        return Objects.equal(name, privilege.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(name);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("\n\t\tPrivilege{");
        sb.append("name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');

        return sb.toString();
    }

}
