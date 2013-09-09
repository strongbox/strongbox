package org.carlspring.strongbox.jaas.permissions;

import org.carlspring.strongbox.jaas.principal.BasePrincipal;

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
