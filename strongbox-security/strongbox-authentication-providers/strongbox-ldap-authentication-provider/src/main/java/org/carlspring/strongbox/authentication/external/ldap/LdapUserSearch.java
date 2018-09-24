package org.carlspring.strongbox.authentication.external.ldap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Przemyslaw Fusik
 */
@XmlAccessorType(XmlAccessType.NONE)
public class LdapUserSearch
{

    @XmlAttribute(name = "search-base")
    private String searchBase;

    @XmlAttribute(name = "search-filter")
    private String searchFilter;

    public String getSearchBase()
    {
        return searchBase;
    }

    public void setSearchBase(final String searchBase)
    {
        this.searchBase = searchBase;
    }

    public String getSearchFilter()
    {
        return searchFilter;
    }

    public void setSearchFilter(final String searchFilter)
    {
        this.searchFilter = searchFilter;
    }
}
