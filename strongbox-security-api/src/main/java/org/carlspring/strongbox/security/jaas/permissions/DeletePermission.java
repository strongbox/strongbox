package org.carlspring.strongbox.security.jaas.permissions;

import org.carlspring.strongbox.security.jaas.principal.BasePrincipal;

/**
 * @author mtodorov
 */
public class DeletePermission
        extends BasePrincipal
{

    public DeletePermission()
    {
    }

    public DeletePermission(String name)
    {
        super(name);
    }

}
