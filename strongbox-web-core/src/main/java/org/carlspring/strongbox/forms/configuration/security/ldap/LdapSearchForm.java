package org.carlspring.strongbox.forms.configuration.security.ldap;

import javax.validation.constraints.NotEmpty;

/**
 * @author Przemyslaw Fusik
 */
public class LdapSearchForm
{

    @NotEmpty
    private String searchBase;

    @NotEmpty
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
