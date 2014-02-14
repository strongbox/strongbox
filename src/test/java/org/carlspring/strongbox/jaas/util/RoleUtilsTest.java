package org.carlspring.strongbox.jaas.util;

import org.carlspring.strongbox.jaas.Role;

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
        List<Role> roles = new ArrayList<Role>();
        roles.add(new Role("Admin", "Admin role"));
        roles.add(new Role("Deployer", "Deployer role"));

        assertEquals("Failed to convert to list!", 2, RoleUtils.toStringList(roles).size());
    }



}
