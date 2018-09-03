package org.carlspring.strongbox.authentication.ldap;

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
public class XmlLdapRolesMapping
{

    @XmlElement(name = "role-mapping")
    private List<XmlLdapRoleMapping> rolesMapping;

    public List<XmlLdapRoleMapping> getRolesMapping()
    {
        return rolesMapping;
    }
}
