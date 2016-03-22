package org.carlspring.strongbox.security.jaas.util;

import org.carlspring.strongbox.security.jaas.Role;

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

    public static List<String> toStringList(Collection<Role> roles)
    {
        List<String> rolesAsStrings = new ArrayList<String>();

        for (Role role : roles)
        {
            rolesAsStrings.add(role.getName());
        }

        return rolesAsStrings;
    }

    public static List<Role> toList(Collection<Role> roles)
    {
        List<Role> rolesList = new ArrayList<Role>();
        rolesList.addAll(roles);

        return rolesList;
    }

}
