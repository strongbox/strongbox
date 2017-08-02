package org.carlspring.strongbox.controllers.security.ldap.support;

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
public class LdapUserDnPatternsResponseEntityBody
{

    @XmlElement(name = "user-dn-pattern")
    private List<String> userDnPatterns;

    LdapUserDnPatternsResponseEntityBody()
    {
    }

    public LdapUserDnPatternsResponseEntityBody(List<String> userDnPatterns)
    {
        this.userDnPatterns = userDnPatterns;
    }
}
