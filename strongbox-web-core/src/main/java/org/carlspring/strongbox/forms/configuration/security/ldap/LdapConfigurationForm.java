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

    @Valid
    private LdapSearchForm userSearch;

    @NotNull
    @Valid
    private LdapSearchForm groupSearch;

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

    public LdapSearchForm getUserSearch()
    {
        return userSearch;
    }

    public void setUserSearch(final LdapSearchForm userSearch)
    {
        this.userSearch = userSearch;
    }

    public LdapSearchForm getGroupSearch()
    {
        return groupSearch;
    }

    public void setGroupSearch(final LdapSearchForm groupSearch)
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
