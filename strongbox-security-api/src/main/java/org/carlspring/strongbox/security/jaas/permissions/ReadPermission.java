package org.carlspring.strongbox.security.jaas.permissions;

import org.carlspring.strongbox.security.jaas.principal.BasePrincipal;

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
