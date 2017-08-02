package org.carlspring.strongbox.configuration;

import javax.xml.bind.annotation.*;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "session-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class SessionConfiguration
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
