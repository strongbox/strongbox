package org.carlspring.strongbox.storage.repository;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author korest
 */
@XmlRootElement(name = "http-connection-pool")
@XmlAccessorType(XmlAccessType.FIELD)
public class HttpConnectionPool
{
    @XmlAttribute(name = "allocated-connections")
    private int allocatedConnections;
    @XmlAttribute(name = "extend-pool-by-factor")
    private double extendPoolByFactor;
    @XmlAttribute(name = "extend-pool-when-usage-above")
    private double extendPoolWhenUsageAbove;

    public int getAllocatedConnections()
    {
        return allocatedConnections;
    }

    public void setAllocatedConnections(int allocatedConnections)
    {
        this.allocatedConnections = allocatedConnections;
    }

    public double getExtendPoolWhenUsageAbove()
    {
        return extendPoolWhenUsageAbove;
    }

    public void setExtendPoolWhenUsageAbove(double extendPoolWhenUsageAbove)
    {
        this.extendPoolWhenUsageAbove = extendPoolWhenUsageAbove;
    }

    public double getExtendPoolByFactor()
    {
        return extendPoolByFactor;
    }

    public void setExtendPoolByFactor(double extendPoolByFactor)
    {
        this.extendPoolByFactor = extendPoolByFactor;
    }
}
