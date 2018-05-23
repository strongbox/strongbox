package org.carlspring.strongbox.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "session-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class MutableSessionConfiguration
        implements Serializable
{
    @XmlAttribute(name= "timeout-seconds")
    private Integer timeoutSeconds = 3600;

    public Integer getTimeoutSeconds()
    {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds)
    {
        this.timeoutSeconds = timeoutSeconds;
    }
}
