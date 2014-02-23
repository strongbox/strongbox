package org.carlspring.strongbox.security.jaas.principal;

import java.security.Principal;

/**
 * @author mtodorov
 */
public class BasePrincipal implements Principal
{

    protected String name;


    public BasePrincipal()
    {
    }

    public BasePrincipal(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String toString()
    {
        return name;
    }

}
