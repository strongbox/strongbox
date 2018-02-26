package org.carlspring.strongbox.converters;

import org.carlspring.strongbox.forms.RoleForm;
import org.carlspring.strongbox.security.Role;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public class RoleFormToRoleConverter
        implements Converter<RoleForm, Role>
{

    @Override
    public Role convert(RoleForm roleForm)
    {
        Role role = new Role(roleForm.getName(), roleForm.getDescription());
        role.setRepository(roleForm.getRepository());
        role.setPrivileges(roleForm.getPrivileges());
        return role;
    }
}
