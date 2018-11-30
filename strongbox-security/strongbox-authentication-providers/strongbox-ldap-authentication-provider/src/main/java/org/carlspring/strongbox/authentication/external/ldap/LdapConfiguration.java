package org.carlspring.strongbox.authentication.external.ldap;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Przemyslaw Fusik
 */
public class LdapConfiguration
{

    private static final int DEFAULT_PORT = 33389;

    private String url;

    private String managerDn;
    
    private String managerPassword;
    
    private String groupSearchBase = "ou=Groups";

    private boolean searchSubtree = true;

    private String groupSearchFilter = "(uniqueMember={0})";

    private String groupRoleAttribute = "cn";

    private String rolePrefix = "";

    private boolean convertToUpperCase;

    private List<LdapRoleMapping> roleMappingList = new ArrayList<>();

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

    public void setManagerPassword(String managerPassword)
    {
        this.managerPassword = managerPassword;
    }

    public String getGroupSearchBase()
    {
        return groupSearchBase;
    }

    public void setGroupSearchBase(String groupSearchBase)
    {
        this.groupSearchBase = groupSearchBase;
    }

    public boolean isSearchSubtree()
    {
        return searchSubtree;
    }

    public void setSearchSubtree(boolean searchSubtree)
    {
        this.searchSubtree = searchSubtree;
    }

    public String getGroupSearchFilter()
    {
        return groupSearchFilter;
    }

    public void setGroupSearchFilter(String groupSearchFilter)
    {
        this.groupSearchFilter = groupSearchFilter;
    }

    public String getGroupRoleAttribute()
    {
        return groupRoleAttribute;
    }

    public void setGroupRoleAttribute(String groupRoleAttribute)
    {
        this.groupRoleAttribute = groupRoleAttribute;
    }

    public String getRolePrefix()
    {
        return rolePrefix;
    }

    public void setRolePrefix(String rolePrefix)
    {
        this.rolePrefix = rolePrefix;
    }

    public boolean isConvertToUpperCase()
    {
        return convertToUpperCase;
    }

    public void setConvertToUpperCase(boolean convertToUpperCase)
    {
        this.convertToUpperCase = convertToUpperCase;
    }

    public List<LdapRoleMapping> getRoleMappingList()
    {
        return roleMappingList;
    }

    public void setRoleMappingList(List<LdapRoleMapping> roleMappingList)
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
