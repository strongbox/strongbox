package org.carlspring.strongbox.controllers.configuration.security.ldap.support;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class LdapGroupSearchResponseEntityBody
{

    private String searchBase;

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

    public String getSearchBase()
    {
        return searchBase;
    }

    public String getSearchFilter()
    {
        return searchFilter;
    }
}
