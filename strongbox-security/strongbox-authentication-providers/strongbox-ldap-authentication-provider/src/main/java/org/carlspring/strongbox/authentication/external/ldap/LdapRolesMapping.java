package org.carlspring.strongbox.authentication.external.ldap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

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
}
