package org.carlspring.strongbox.configuration;

import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @author Przemyslaw Fusik
 */
@Embeddable
@XmlRootElement(name = "session-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class SessionConfiguration
        implements Serializable
{
    @XmlAttribute(name= "timeout-seconds")
    private Integer timeoutSeconds = 30;

    public Integer getTimeoutSeconds()
    {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds)
    {
        this.timeoutSeconds = timeoutSeconds;
    }
}
