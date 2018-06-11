package org.carlspring.strongbox.converters;

import org.carlspring.strongbox.forms.RoleForm;
import org.carlspring.strongbox.authorization.dto.RoleDto;

import org.springframework.core.convert.converter.Converter;

/**
 * @author Pablo Tirado
 */
public class RoleFormToRoleConverter
        implements Converter<RoleForm, RoleDto>
{

    @Override
    public RoleDto convert(RoleForm roleForm)
    {
        RoleDto role = new RoleDto(roleForm.getName(), roleForm.getDescription());
        role.setRepository(roleForm.getRepository());
        role.setPrivileges(roleForm.getPrivileges());
        return role;
    }
}
