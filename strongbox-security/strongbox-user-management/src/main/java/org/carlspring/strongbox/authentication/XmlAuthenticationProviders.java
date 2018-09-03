package org.carlspring.strongbox.authentication;

import org.carlspring.strongbox.authentication.ldap.XmlLdapConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "authentication-providers")
@XmlAccessorType(XmlAccessType.NONE)
public class XmlAuthenticationProviders
{

    @XmlElement
    private XmlAuthenticators authenticators;

    @XmlElement(name = "extra-beans")
    private XmlAuthenticationExtraBeans extraBeans;

    @XmlElement(name = "ldap")
    private XmlLdapConfiguration ldapConfiguration;

    public XmlAuthenticators getAuthenticators()
    {
        return authenticators;
    }

    public XmlAuthenticationExtraBeans getExtraBeans()
    {
        return extraBeans;
    }

    public XmlLdapConfiguration getLdapConfiguration()
    {
        return ldapConfiguration;
    }
}
