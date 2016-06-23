package org.carlspring.strongbox.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;

import org.springframework.core.io.Resource;

/**
 * @author mtodorov
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class ServerConfiguration<T>
{

    @XmlTransient
    private Resource resource;


    public ServerConfiguration()
    {
    }

    public Resource getResource()
    {
        return resource;
    }

    public void setResource(Resource resource)
    {
        this.resource = resource;
    }

}
