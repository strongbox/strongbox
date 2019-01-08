package org.carlspring.strongbox.authentication.external.ldap;

import org.carlspring.strongbox.authentication.support.ExternalRolesMappingFactoryBean;

public class LdapRolesMappingFactoryBean extends ExternalRolesMappingFactoryBean
{

    public LdapRolesMappingFactoryBean()
    {
        super("ldap");
    }
    
}
