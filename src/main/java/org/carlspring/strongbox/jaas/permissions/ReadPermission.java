package org.carlspring.strongbox.jaas.permissions;

import org.carlspring.strongbox.jaas.principal.BasePrincipal;

/**
 * @author mtodorov
 */
public class ReadPermission
        extends BasePrincipal
{

    public ReadPermission()
    {
    }

    public ReadPermission(String name)
    {
        super(name);
    }

}
