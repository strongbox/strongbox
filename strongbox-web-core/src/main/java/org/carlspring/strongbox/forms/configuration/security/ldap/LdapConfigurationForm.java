package org.carlspring.strongbox.forms.configuration.security.ldap;

import org.carlspring.strongbox.validation.LdapUri;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author Przemyslaw Fusik
 */
public class LdapConfigurationForm
{

    @LdapUri
    @NotEmpty
    private String url;

    private String managerDn;

    private String managerPassword;

    private Map<String, String> rolesMapping;

    private List<String> userDnPatterns;

    /**
     * If set to true, a subtree scope search will be performed. If false a single-level
     * search is used.
     */
    private boolean searchSubtree;

    @NotNull
    @Valid
    private LdapUserSearchForm userSearch;

    @NotNull
    @Valid
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

    public String getUrl()
    {
        return url;
    }

    public void setUrl(final String url)
    {
        this.url = url;
    }

    public String getManagerDn()
    {
        return managerDn;
    }

    public void setManagerDn(final String managerDn)
    {
        this.managerDn = managerDn;
    }

    public String getManagerPassword()
    {
        return managerPassword;
    }

    public void setManagerPassword(final String managerPassword)
    {
        this.managerPassword = managerPassword;
    }

}
