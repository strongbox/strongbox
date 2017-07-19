package org.carlspring.strongbox.controllers.security.ldap.support;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "groupSearch")
@XmlAccessorType(XmlAccessType.NONE)
public class LdapGroupSearchResponseEntityBody
{

    @XmlElement(name = "searchBase")
    private String searchBase;

    @XmlElement(name = "searchFilter")
    private String searchFilter;

    LdapGroupSearchResponseEntityBody searchBase(String searchBase)
    {
        this.searchBase = searchBase;
        return this;
    }

    LdapGroupSearchResponseEntityBody searchFilter(String searchFilter)
    {
        this.searchFilter = searchFilter;
        return this;
    }
}
