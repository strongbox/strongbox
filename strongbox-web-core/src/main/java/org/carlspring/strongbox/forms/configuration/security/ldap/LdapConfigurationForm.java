package org.carlspring.strongbox.forms.configuration.security.ldap;

import java.util.List;
import java.util.Map;

/**
 * @author Przemyslaw Fusik
 */
public class LdapConfigurationForm
{

    private Map<String, String> rolesMapping;

    private List<String> userDnPatterns;

    private LdapUserSearchForm userSearch;

    private LdapGroupSearchForm groupSearch;

    public Map<String, String> getRolesMapping()
    {
        return rolesMapping;
    }

    public void setRolesMapping(final Map<String, String> rolesMapping)
    {
        this.rolesMapping = rolesMapping;
    }

    public List<String> getUserDnPatterns()
    {
        return userDnPatterns;
    }

    public void setUserDnPatterns(final List<String> userDnPatterns)
    {
        this.userDnPatterns = userDnPatterns;
    }

    public LdapUserSearchForm getUserSearch()
    {
        return userSearch;
    }

    public void setUserSearch(final LdapUserSearchForm userSearch)
    {
        this.userSearch = userSearch;
    }

    public LdapGroupSearchForm getGroupSearch()
    {
        return groupSearch;
    }

    public void setGroupSearch(final LdapGroupSearchForm groupSearch)
    {
        this.groupSearch = groupSearch;
    }
}
