package org.carlspring.strongbox.security.jaas.util;

import org.carlspring.strongbox.security.jaas.Privilege;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author mtodorov
 */
public class PrivilegeUtils
{

    private PrivilegeUtils() {
    }

    public static List<String> toStringList(Collection<Privilege> privileges)
    {
        List<String> privilegesAsStrings = new ArrayList<String>();

        for (Privilege privilege : privileges)
        {
            privilegesAsStrings.add(privilege.getName());
        }

        return privilegesAsStrings;
    }

    public static List<Privilege> toList(Collection<Privilege> privileges)
    {
        List<Privilege> privilegesList = new ArrayList<Privilege>();
        privilegesList.addAll(privileges);
        
        return privilegesList;
    }
    
}
