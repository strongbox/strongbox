package org.carlspring.strongbox.authorization.util;


import org.carlspring.strongbox.authorization.dto.RoleDto;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

        assertEquals(2, RoleUtils.toStringList(roles).size(), "Failed to convert to list!");
    }


}
