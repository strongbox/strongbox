package org.carlspring.strongbox.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "cors-configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class MutableCorsConfiguration
        implements Serializable
{

    @XmlElementWrapper(name = "allowed-origins")
    @XmlElement(name = "allowed-origin")
    private List<String> allowedOrigins;

    @XmlElementWrapper(name = "allowed-methods")
    @XmlElement(name = "allowed-method")
    private List<String> allowedMethods;

    @XmlElementWrapper(name = "allowed-headers")
    @XmlElement(name = "allowed-header")
    private List<String> allowedHeaders;

    @XmlElementWrapper(name = "exposed-headers")
    @XmlElement(name = "exposed-header")
    private List<String> exposedHeaders;

    @XmlAttribute(name = "allowed-credentials")
    private Boolean allowCredentials;

    @XmlAttribute(name = "max-age")
    private Long maxAge;

    public List<String> getAllowedOrigins()
    {
        return allowedOrigins;
    }

    public void setAllowedOrigins(final List<String> allowedOrigins)
    {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedMethods()
    {
        return allowedMethods;
    }

    public void setAllowedMethods(final List<String> allowedMethods)
    {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders()
    {
        return allowedHeaders;
    }

    public void setAllowedHeaders(final List<String> allowedHeaders)
    {
        this.allowedHeaders = allowedHeaders;
    }

    public List<String> getExposedHeaders()
    {
        return exposedHeaders;
    }

    public void setExposedHeaders(final List<String> exposedHeaders)
    {
        this.exposedHeaders = exposedHeaders;
    }

    public Boolean getAllowCredentials()
    {
        return allowCredentials;
    }

    public void setAllowCredentials(final Boolean allowCredentials)
    {
        this.allowCredentials = allowCredentials;
    }

    public Long getMaxAge()
    {
        return maxAge;
    }

    public void setMaxAge(final Long maxAge)
    {
        this.maxAge = maxAge;
    }
}
