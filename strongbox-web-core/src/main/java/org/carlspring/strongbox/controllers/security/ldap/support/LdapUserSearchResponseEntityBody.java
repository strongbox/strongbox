package org.carlspring.strongbox.controllers.security.ldap.support;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "userSearch")
@XmlAccessorType(XmlAccessType.NONE)
public class LdapUserSearchResponseEntityBody
{

    @XmlElement(name = "searchBase")
    private String searchBase;

    @XmlElement(name = "searchFilter")
    private String searchFilter;

    LdapUserSearchResponseEntityBody searchBase(String searchBase)
    {
        this.searchBase = searchBase;
        return this;
    }

    LdapUserSearchResponseEntityBody searchFilter(String searchFilter)
    {
        this.searchFilter = searchFilter;
        return this;
    }
}
