package org.carlspring.strongbox.authentication.registry.support.xml.in;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlAuthenticator
{

    @XmlAttribute(name = "class")
    private String clazz;

    @XmlAttribute(name = "component-scan-base-packages")
    private String componentScanBasePackages;

    public String getClazz()
    {
        return clazz;
    }

    public String getComponentScanBasePackages()
    {
        return componentScanBasePackages;
    }
}
