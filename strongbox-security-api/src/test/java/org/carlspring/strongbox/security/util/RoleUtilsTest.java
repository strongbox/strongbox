package org.carlspring.strongbox.security.util;

import org.carlspring.strongbox.security.Role;

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
        List<Role> roles = new ArrayList<>();
        roles.add(new Role("Admin", "Admin role"));
        roles.add(new Role("Deployer", "Deployer role"));

        assertEquals("Failed to convert to list!", 2, RoleUtils.toStringList(roles).size());
    }



}
