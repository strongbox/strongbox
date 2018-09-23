package org.carlspring.strongbox.authentication.external.ldap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "user-dn-patterns")
@XmlAccessorType(XmlAccessType.NONE)
public class LdapUserDnPatterns
{

    @XmlElement(name = "user-dn-pattern")
    private List<LdapUserDnPattern> userDnPattern;

    public List<LdapUserDnPattern> getUserDnPattern()
    {
        return userDnPattern;
    }

    public void setUserDnPattern(final List<LdapUserDnPattern> userDnPattern)
    {
        this.userDnPattern = userDnPattern;
    }

    public void add(final LdapUserDnPattern pattern)
    {
        if (userDnPattern == null)
        {
            userDnPattern = new ArrayList<>();
        }
        userDnPattern.add(pattern);
    }
}
