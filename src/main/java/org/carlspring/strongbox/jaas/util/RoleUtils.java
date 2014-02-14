package org.carlspring.strongbox.jaas.util;

import org.carlspring.strongbox.jaas.Role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author mtodorov
 */
public class RoleUtils
{

    public static List<String> toStringList(Collection<Role> roles)
    {
        List<String> rolesAsStrings = new ArrayList<String>();

        for (Role role : roles)
        {
            rolesAsStrings.add(role.getName());
        }

        return rolesAsStrings;
    }

}
