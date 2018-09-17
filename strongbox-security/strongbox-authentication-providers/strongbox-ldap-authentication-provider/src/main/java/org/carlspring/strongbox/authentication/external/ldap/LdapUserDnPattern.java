package org.carlspring.strongbox.authentication.external.ldap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "user-dn-pattern")
@XmlAccessorType(XmlAccessType.NONE)
public class LdapUserDnPattern
{

    @XmlAttribute(required = true)
    private String value;

    public String getValue()
    {
        return value;
    }
}
