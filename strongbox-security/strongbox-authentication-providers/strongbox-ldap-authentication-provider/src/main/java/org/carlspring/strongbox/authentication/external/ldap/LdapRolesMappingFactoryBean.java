package org.carlspring.strongbox.authentication.external.ldap;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

public class LdapRolesMappingFactoryBean extends AbstractFactoryBean<Map<String, String>>
{

    @Inject
    private Environment env;

    @Override
    public Class<?> getObjectType()
    {
        return Map.class;
    }

    @Override
    protected Map<String, String> createInstance()
        throws Exception
    {
        Map<String, String> roleMappingMap = new HashMap<>();

        PropertySource<?> propertySource = ((AbstractEnvironment) env).getPropertySources()
                                                                      .get("strongbox-authentication-providers");
        String[] propertyNames = ((EnumerablePropertySource<?>) propertySource).getPropertyNames();

        Map<String, String> ldapRolesMap = new HashMap<>();
        Map<String, String> strongboxRolesMap = new HashMap<>();

        for (String propertyName : propertyNames)
        {
            if (!propertyName.startsWith("strongbox.authentication.ldap.rolesMapping"))
            {
                continue;
            }
            if (propertyName.endsWith("ldapRole"))
            {
                ldapRolesMap.put(propertyName.replace("strongbox.authentication.ldap.rolesMapping", ""),
                                 env.getProperty(propertyName));
            }
            else if (propertyName.endsWith("strongboxRole"))
            {
                strongboxRolesMap.put(propertyName.replace("strongbox.authentication.ldap.rolesMapping", ""),
                                      env.getProperty(propertyName));
            }
        }
        
        for (Entry<String, String> ldapRoleEntry : ldapRolesMap.entrySet())
        {
            String ldapRole = ldapRoleEntry.getValue();
            String strongboxRole = strongboxRolesMap.get(ldapRoleEntry.getKey().replace("ldapRole","strongboxRole"));
            
            roleMappingMap.put(ldapRole, strongboxRole);
        }

        return roleMappingMap;
    }

}
