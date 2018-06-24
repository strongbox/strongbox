package org.carlspring.strongbox.authorization.util;


import org.carlspring.strongbox.authorization.dto.RoleDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author mtodorov
 */
public class RoleUtils
{

    private RoleUtils()
    {
    }

    public static List<String> toStringList(Collection<RoleDto> roles)
    {
        List<String> rolesAsStrings = new ArrayList<>();

        for (RoleDto role : roles)
        {
            rolesAsStrings.add(role.getName());
        }

        return rolesAsStrings;
    }

    public static List<RoleDto> toList(Collection<RoleDto> roles)
    {
        List<RoleDto> rolesList = new ArrayList<>();
        rolesList.addAll(roles);

        return rolesList;
    }

}
