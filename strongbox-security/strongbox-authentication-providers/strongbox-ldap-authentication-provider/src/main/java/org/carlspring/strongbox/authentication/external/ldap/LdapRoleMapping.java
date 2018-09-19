package org.carlspring.strongbox.authentication.external.ldap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "role-mapping")
@XmlAccessorType(XmlAccessType.NONE)
public class LdapRoleMapping
{

    @XmlAttribute(name = "ldap-role")
    private String ldapRole;

    @XmlAttribute(name = "strongbox-role")
    private String strongboxRole;

    public String getLdapRole()
    {
        return ldapRole;
    }

    public void setLdapRole(final String ldapRole)
    {
        this.ldapRole = ldapRole;
    }

    public String getStrongboxRole()
    {
        return strongboxRole;
    }

    public void setStrongboxRole(final String strongboxRole)
    {
        this.strongboxRole = strongboxRole;
    }
}
