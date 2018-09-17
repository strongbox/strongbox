package org.carlspring.strongbox.authentication.external.ldap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlAccessorType(XmlAccessType.NONE)
public class LdapBindAuthenticator
{

    @XmlElement(name = "user-search")
    private LdapUserSearch userSearch;

    @XmlElement(name = "user-dn-patterns")
    private LdapUserDnPatterns userDnPatterns;

    public LdapUserSearch getUserSearch()
    {
        return userSearch;
    }

    public LdapUserDnPatterns getUserDnPatterns()
    {
        return userDnPatterns;
    }
}
