package org.carlspring.strongbox.controllers.security.ldap.support;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "userDnPatterns")
@XmlAccessorType(XmlAccessType.NONE)
public class LdapUserDnPatternsXmlHolder
{

    @XmlElement(name = "userDnPattern")
    private List<String> userDnPatterns;

    LdapUserDnPatternsXmlHolder()
    {
    }

    public LdapUserDnPatternsXmlHolder(List<String> userDnPatterns)
    {
        this.userDnPatterns = userDnPatterns;
    }
}
