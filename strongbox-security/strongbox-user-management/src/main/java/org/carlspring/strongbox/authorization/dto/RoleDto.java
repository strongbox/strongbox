package org.carlspring.strongbox.authorization.dto;

import java.io.Serializable;

import org.carlspring.strongbox.users.domain.Privileges;
import org.carlspring.strongbox.users.dto.UserAccessModelDto;

import com.google.common.base.Objects;

/**
 * @author mtodorov
 */
public class RoleDto
        implements Serializable, RoleReadContract
{

    private String name;

    private String description;

    private UserAccessModelDto accessModel;


    public RoleDto()
    {
    }

    public RoleDto(String name,
                   String description,
                   UserAccessModelDto accessModel)
    {
        this.name = name;
        this.description = description;
        this.accessModel = accessModel;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public UserAccessModelDto getAccessModel()
    {
        return accessModel;
    }

    public void setAccessModel(UserAccessModelDto accessModel)
    {
        this.accessModel = accessModel;
    }

    public void addPrivilege(Privileges p)
    {
        accessModel.getApiAuthorities().add(p);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        RoleDto role = (RoleDto) o;
        return Objects.equal(accessModel, role.accessModel);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(accessModel);
    }
    
    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("\n\t\tRole{");
        sb.append("name='").append(name).append('\'');
        sb.append(", description='").append(description);
        sb.append('}');

        return sb.toString();
    }

}
