package org.carlspring.strongbox.controllers.configuration.security.ldap.support;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LdapUserDnPatternsResponseEntityBody
{

    private List<String> userDnPatterns;

    LdapUserDnPatternsResponseEntityBody()
    {
    }

    public LdapUserDnPatternsResponseEntityBody(List<String> userDnPatterns)
    {
        this.userDnPatterns = userDnPatterns;
    }

    public List<String> getUserDnPatterns()
    {
        return userDnPatterns;
    }
}
