package org.carlspring.strongbox.authentication.api.ldap;

import org.carlspring.strongbox.authentication.support.ExternalRolesMappingFactoryBean;

public class LdapRolesMappingFactoryBean extends ExternalRolesMappingFactoryBean
{

    public LdapRolesMappingFactoryBean()
    {
        super("ldap");
    }
    
}
