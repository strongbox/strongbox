package org.carlspring.strongbox.authentication.external.ldap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "ldap")
@XmlAccessorType(XmlAccessType.NONE)
public class XmlLdapConfiguration
{

    @XmlAttribute
    private String url;

    /**
     * For embedded server. Should not be used in production.
     */
    @XmlAttribute
    private String ldif;

    @XmlElement(name = "roles-mapping")
    private XmlLdapRolesMapping rolesMapping;

    @XmlElement
    private XmlLdapBindAuthenticator authenticator;

    @XmlElement(name = "authorities-populator")
    private XmlLdapAuthoritiesPopulator authoritiesPopulator;

    public String getUrl()
    {
        return url;
    }

    public String getLdif()
    {
        return ldif;
    }

    public XmlLdapBindAuthenticator getAuthenticator()
    {
        return authenticator;
    }

    public XmlLdapAuthoritiesPopulator getAuthoritiesPopulator()
    {
        return authoritiesPopulator;
    }

}
