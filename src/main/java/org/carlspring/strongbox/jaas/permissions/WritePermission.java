package org.carlspring.strongbox.jaas.permissions;

import org.carlspring.strongbox.jaas.principal.BasePrincipal;

/**
 * @author mtodorov
 */
public class WritePermission
        extends BasePrincipal
{

    public WritePermission()
    {
    }

    public WritePermission(String name)
    {
        super(name);
    }

}
