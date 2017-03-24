package org.carlspring.strongbox.storage.repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author korest
 */
@XmlRootElement(name = "http-connection-pool")
@XmlAccessorType(XmlAccessType.FIELD)
public class HttpConnectionPool
        implements Serializable
{

    /**
     * Added to avoid a runtime error whereby the detachAll property is checked for existence but not actually used.
     */
    @JsonIgnore
    protected String detachAll;

    @XmlAttribute(name = "allocated-connections")
    private int allocatedConnections;

    public int getAllocatedConnections()
    {
        return allocatedConnections;
    }

    public void setAllocatedConnections(int allocatedConnections)
    {
        this.allocatedConnections = allocatedConnections;
    }

    public String getDetachAll()
    {
        return detachAll;
    }

    public void setDetachAll(String detachAll)
    {
        this.detachAll = detachAll;
    }
}
