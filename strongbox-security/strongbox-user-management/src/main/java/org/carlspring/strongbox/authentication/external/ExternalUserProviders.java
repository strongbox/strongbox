package org.carlspring.strongbox.authentication.external;

import org.carlspring.strongbox.authentication.external.ldap.XmlLdapConfiguration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "external-user-providers")
@XmlAccessorType(XmlAccessType.NONE)
public class ExternalUserProviders
{
    @XmlElement(name = "ldap")
    private XmlLdapConfiguration ldapConfiguration;

    public XmlLdapConfiguration getLdapConfiguration()
    {
        return ldapConfiguration;
    }
}
