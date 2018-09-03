package org.carlspring.strongbox.authentication.ldap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlAccessorType(XmlAccessType.NONE)
public class XmlLdapBindAuthenticator
{

    @XmlElement(name = "user-search")
    private XmlLdapUserSearch userSearch;

    @XmlElement(name = "user-dn-patterns")
    private XmlLdapUserDnPatterns userDnPatterns;

    public XmlLdapUserSearch getUserSearch()
    {
        return userSearch;
    }

    public XmlLdapUserDnPatterns getUserDnPatterns()
    {
        return userDnPatterns;
    }
}
