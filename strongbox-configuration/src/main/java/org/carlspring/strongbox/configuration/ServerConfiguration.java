package org.carlspring.strongbox.configuration;

import java.io.Serializable;

/**
 * @author mtodorov
 * @author Pablo Tirado
 */
public abstract class ServerConfiguration
        implements Serializable
{

    protected String id;

    public ServerConfiguration()
    {
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
}
