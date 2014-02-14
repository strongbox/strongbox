package org.carlspring.strongbox.jaas.util;

import org.carlspring.strongbox.jaas.Privilege;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author mtodorov
 */
public class PrivilegeUtils
{

    public static List<String> toStringList(Collection<Privilege> privileges)
    {
        List<String> privilegesAsStrings = new ArrayList<String>();

        for (Privilege privilege : privileges)
        {
            privilegesAsStrings.add(privilege.getName());
        }

        return privilegesAsStrings;
    }

}
