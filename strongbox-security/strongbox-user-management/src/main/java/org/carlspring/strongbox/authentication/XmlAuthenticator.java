package org.carlspring.strongbox.authentication;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "authenticator")
@XmlAccessorType(XmlAccessType.NONE)
public class XmlAuthenticator
{

    @XmlAttribute(name = "class-name")
    private String className;

    public String getClassName()
    {
        return className;
    }
}
