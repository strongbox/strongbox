package org.carlspring.strongbox.authentication.external.ldap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

/**
 * @author Przemyslaw Fusik
 */
@XmlRootElement(name = "roles-mapping")
@XmlAccessorType(XmlAccessType.NONE)
public class LdapRolesMapping
{

    @XmlElement(name = "role-mapping")
    private List<LdapRoleMapping> rolesMapping;

    public List<LdapRoleMapping> getRolesMapping()
    {
        return rolesMapping;
    }

    public void setRolesMapping(final List<LdapRoleMapping> rolesMapping)
    {
        this.rolesMapping = rolesMapping;
    }

    public Map<String, String> asMap()
    {
        if (CollectionUtils.isEmpty(rolesMapping))
        {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        rolesMapping.stream().forEach(rm -> result.put(rm.getLdapRole(), rm.getStrongboxRole()));
        return result;
    }
}
