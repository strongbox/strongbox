package org.carlspring.strongbox.authentication.external.ldap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Przemyslaw Fusik
 */
@XmlAccessorType(XmlAccessType.NONE)
public class XmlLdapAuthoritiesPopulator
{

    @XmlAttribute(name = "group-search-base")
    private String groupSearchBase = "ou=Groups";

    @XmlAttribute(name = "search-subtree")
    private boolean searchSubtree = true;

    @XmlAttribute(name = "group-search-filter")
    private String groupSearchFilter = "(uniqueMember={0})";

    @XmlAttribute(name = "group-role-attribute")
    private String groupRoleAttribute = "cn";

    @XmlAttribute(name = "role-prefix")
    private String rolePrefix = "";

    @XmlAttribute(name = "convert-to-upper-case")
    private boolean convertToUpperCase;

    public String getGroupSearchBase()
    {
        return groupSearchBase;
    }

    public void setGroupSearchBase(final String groupSearchBase)
    {
        this.groupSearchBase = groupSearchBase;
    }

    public boolean isSearchSubtree()
    {
        return searchSubtree;
    }

    public void setSearchSubtree(final boolean searchSubtree)
    {
        this.searchSubtree = searchSubtree;
    }

    public String getGroupSearchFilter()
    {
        return groupSearchFilter;
    }

    public void setGroupSearchFilter(final String groupSearchFilter)
    {
        this.groupSearchFilter = groupSearchFilter;
    }

    public String getGroupRoleAttribute()
    {
        return groupRoleAttribute;
    }

    public void setGroupRoleAttribute(final String groupRoleAttribute)
    {
        this.groupRoleAttribute = groupRoleAttribute;
    }

    public String getRolePrefix()
    {
        return rolePrefix;
    }

    public void setRolePrefix(final String rolePrefix)
    {
        this.rolePrefix = rolePrefix;
    }

    public boolean isConvertToUpperCase()
    {
        return convertToUpperCase;
    }

    public void setConvertToUpperCase(final boolean convertToUpperCase)
    {
        this.convertToUpperCase = convertToUpperCase;
    }
}
