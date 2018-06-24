package org.carlspring.strongbox.authorization.util;

import org.carlspring.strongbox.authorization.dto.PrivilegeDto;

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
        List<PrivilegeDto> privileges = new ArrayList<>();
        privileges.add(new PrivilegeDto("read", "Read permission"));
        privileges.add(new PrivilegeDto("write", "Write permission"));
        privileges.add(new PrivilegeDto("deploy", "Deploy permission"));

        assertEquals("Failed to convert to list!", 3, PrivilegeUtils.toStringList(privileges).size());
    }

}
