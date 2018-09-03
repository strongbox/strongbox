package org.carlspring.strongbox.authentication.ldap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "user-dn-patterns")
@XmlAccessorType(XmlAccessType.NONE)
public class XmlLdapUserDnPatterns
{
    @XmlElement(name = "user-dn-pattern")
    private List<XmlLdapUserDnPattern> userDnPattern;

    public List<XmlLdapUserDnPattern> getUserDnPattern()
    {
        return userDnPattern;
    }
}
