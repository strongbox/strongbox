package org.carlspring.strongbox.authentication.external.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotEmpty;

import org.carlspring.strongbox.authentication.support.ExternalRoleMapping;
import org.carlspring.strongbox.validation.LdapUri;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * @author Przemyslaw Fusik
 */
public class LdapConfiguration
{

    @LdapUri
    @NotEmpty
    private String url;

    private String managerDn;

    private String managerPassword;

    private LdapAuthoritiesConfiguration authoritiesConfiguration = new LdapAuthoritiesConfiguration();

    private LdapGroupSearch groupSearch = new LdapGroupSearch();

    private List<ExternalRoleMapping> roleMappingList = new ArrayList<>();

    private List<String> userDnPatternList = new ArrayList<>();

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getManagerDn()
    {
        return managerDn;
    }

    public void setManagerDn(String managerDn)
    {
        this.managerDn = managerDn;
    }

    public String getManagerPassword()
    {
        return managerPassword;
    }

    @JsonProperty("authorities")
    public LdapAuthoritiesConfiguration getAuthoritiesConfiguration()
    {
        return authoritiesConfiguration;
    }

    public void setAuthoritiesConfiguration(LdapAuthoritiesConfiguration authoritiesConfiguration)
    {
        this.authoritiesConfiguration = authoritiesConfiguration;
    }

    @JsonUnwrapped
    public LdapGroupSearch getGroupSearch()
    {
        return groupSearch;
    }

    public void setGroupSearch(LdapGroupSearch groupSearch)
    {
        this.groupSearch = groupSearch;
    }

    public void setManagerPassword(String managerPassword)
    {
        this.managerPassword = managerPassword;
    }

    public List<ExternalRoleMapping> getRoleMappingList()
    {
        return roleMappingList;
    }

    public void setRoleMappingList(List<ExternalRoleMapping> roleMappingList)
    {
        this.roleMappingList = roleMappingList;
    }

    public List<String> getUserDnPatternList()
    {
        return userDnPatternList;
    }

    public void setUserDnPatternList(List<String> userDnPatternList)
    {
        this.userDnPatternList = userDnPatternList;
    }

}
