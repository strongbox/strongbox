package org.carlspring.strongbox.authorization.dto;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

import com.google.common.base.Objects;

/**
 * @author mtodorov
 */
@XmlRootElement(name = "privilege")
@XmlAccessorType(XmlAccessType.FIELD)
public class PrivilegeDto
        implements Serializable, PrivelegieReadContract
{

    @XmlElement(required = true)
    private String name;

    private String description;


    public PrivilegeDto()
    {
    }

    public PrivilegeDto(String name,
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
        PrivilegeDto privilege = (PrivilegeDto) o;
        return Objects.equal(name, privilege.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(name);
    }

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.authorization.dto.PrivelegieReadContract#getName()
     */
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see org.carlspring.strongbox.authorization.dto.PrivelegieReadContract#getDescription()
     */
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
        final StringBuilder sb = new StringBuilder("Privilege{");
        sb.append("name='")
          .append(name)
          .append('\'');
        sb.append(", description='")
          .append(description)
          .append('\'');
        sb.append('}');

        return sb.toString();
    }

}
