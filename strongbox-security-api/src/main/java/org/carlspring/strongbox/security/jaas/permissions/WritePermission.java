package org.carlspring.strongbox.security.jaas.permissions;

import org.carlspring.strongbox.security.jaas.principal.BasePrincipal;

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
