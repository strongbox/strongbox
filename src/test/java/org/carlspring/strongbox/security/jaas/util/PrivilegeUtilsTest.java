package org.carlspring.strongbox.security.jaas.util;

import org.carlspring.strongbox.security.jaas.Privilege;
import org.carlspring.strongbox.security.jaas.util.PrivilegeUtils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author mtodorov
 */
public class PrivilegeUtilsTest
{

    @Test
    public void testPrivilegesToString()
    {
        List<Privilege> privileges = new ArrayList<Privilege>();
        privileges.add(new Privilege("read", "Read permission"));
        privileges.add(new Privilege("write", "Write permission"));
        privileges.add(new Privilege("deploy", "Deploy permission"));

        assertEquals("Failed to convert to list!", 3, PrivilegeUtils.toStringList(privileges).size());
    }

}
