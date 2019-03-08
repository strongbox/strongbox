package org.carlspring.strongbox.authorization.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
public class PrivilegeDto
        implements Serializable, PrivelegieReadContract
{

    private String name;

    private String description;

    @JsonCreator
    public PrivilegeDto(@JsonProperty(value = "name", required = true) String name,
                        @JsonProperty(value = "description") String description)
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
