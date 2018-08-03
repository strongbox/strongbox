package org.carlspring.strongbox.forms.configuration.security.ldap;

/**
 * @author Przemyslaw Fusik
 */
public class LdapGroupSearchForm
{

    private String searchBase;

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
