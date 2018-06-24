package org.carlspring.strongbox.authorization.util;


import org.carlspring.strongbox.authorization.dto.PrivilegeDto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author mtodorov
 */
public class PrivilegeUtils
{

    private PrivilegeUtils()
    {
    }

    public static List<String> toStringList(Collection<PrivilegeDto> privileges)
    {
        List<String> privilegesAsStrings = new ArrayList<>();

        for (PrivilegeDto privilege : privileges)
        {
            privilegesAsStrings.add(privilege.getName());
        }

        return privilegesAsStrings;
    }

    public static List<PrivilegeDto> toList(Collection<PrivilegeDto> privileges)
    {
        List<PrivilegeDto> privilegesList = new ArrayList<>();
        privilegesList.addAll(privileges);

        return privilegesList;
    }

}
