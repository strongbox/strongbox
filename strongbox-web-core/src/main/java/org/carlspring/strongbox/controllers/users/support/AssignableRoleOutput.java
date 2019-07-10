package org.carlspring.strongbox.controllers.users.support;

import org.carlspring.strongbox.authorization.domain.RoleData;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude()
public class AssignableRoleOutput
{

    private String name;
    private String description;

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

    public static AssignableRoleOutput fromRole(RoleData role)
    {
        final AssignableRoleOutput output = new AssignableRoleOutput();
        output.setName(role.getName());
        output.setDescription(role.getDescription());

        return output;
    }
}
