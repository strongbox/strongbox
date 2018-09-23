package org.carlspring.strongbox.forms.configuration.security.ldap;

import javax.validation.constraints.NotEmpty;

/**
 * @author Przemyslaw Fusik
 */
public class LdapGroupSearchForm
{

    /**
     * The base DN from which the search for group membership should be performed
     */
    @NotEmpty
    private String searchBase;

    /**
     * The pattern to be used for the user search. {0} is the user's DN
     */
    @NotEmpty
    private String searchFilter;

    /**
     * The ID of the attribute which contains the role name for a group
     */
    private String groupRoleAttribute;

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

    public String getGroupRoleAttribute()
    {
        return groupRoleAttribute;
    }

    public void setGroupRoleAttribute(final String groupRoleAttribute)
    {
        this.groupRoleAttribute = groupRoleAttribute;
    }
}
