package org.carlspring.strongbox.authorization.util;


import org.carlspring.strongbox.authorization.dto.RoleDto;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author mtodorov
 */
public class RoleUtilsTest
{

    @Test
    public void testRolesToString()
    {
        List<RoleDto> roles = new ArrayList<>();
        roles.add(new RoleDto("Admin", "Admin role"));
        roles.add(new RoleDto("Deployer", "Deployer role"));

        assertEquals("Failed to convert to list!", 2, RoleUtils.toStringList(roles).size());
    }


}
