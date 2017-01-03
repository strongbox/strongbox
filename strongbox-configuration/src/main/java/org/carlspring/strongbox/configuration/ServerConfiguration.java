package org.carlspring.strongbox.configuration;

import org.carlspring.strongbox.data.domain.GenericEntity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ServerConfiguration
        extends GenericEntity
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
